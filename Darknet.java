package simsalabim;
import java.util.*;
import java.io.*;
/**
 * Simulates a Darknet using the algorithm described in
 * 
 */

public class Darknet extends Simulator<DarknetNode, CircleKey> {

	int INITIAL_SWITCHES;
	int MAINT_SWITCHES;
	int HTL;
	int STORE_SIZE;
	int CACHE_SIZE;
	int N_BEST;
	int RAND_STEPS;
	int OPENNET_MIN_SUCCESS_BETWEEN_DROP_CONNS;
	float OPENNET_RESET_PATH_FOLDING_PROB;
	int OPENNET_DROP_MIN_AGE;              // Has to be tuned for churn... 
	int OPENNET_MIN_TIME_BEFORE_OFFERS;
	int OPENNET_MINPEERS;
	int OPENNET_MAXPEERS;
	int MAXPEERS;                          // Max num of online peers for a node (a person may know more than this)
	int RANDOM_GROWTH_MODEL;	       // How to add nodes, growing from the underlying social network
	int VARDIST_RESOLUTION;		       // Resolution of computing distribution distance for locations
	float OPENNET_NODES_SWAP_PROB;         // Should nodes in opennet eval swaps?
	float HYBRID_NODES_SWAP_PROB;          // Should hybrid nodes eval swaps?
	float LEAVE_PERM_PROB;                 // Whether to leave permanent or stay dormant

	float DARKLINKS_ONLY_PROB;
	float RECACHE_PROB;
	float RELINK_PROB;

	boolean first_split = false;           // Did the network undergo a first split? Debugging switching.

	HashMap<Integer, Person> people;
	TreeSet<Person> ts = new TreeSet<Person>();
	HashMap<Person, DarknetNode> dormant = new HashMap<Person, DarknetNode>();
	// For several graph partitions
	int person_id_offset = 0;
	Person[] people1;
	Person[] people2;	
	
	int size;
	int n_darknet_total;
	int n_opennet_total;
	int n_opennet_active;
	int n_darknet_active;

	/*
	 * Use (load) single graph:
	 *
	 * Use dual graphs (as the Kleinberg model):
	 *
	 * Darknet dual size1 outdeg1 size2 outdeg2 cut
	 * where
	 *     size = #nodes
	 *     outdeg = #edges (undirected) added to each node in each network
	 *     cut = #edges between networks
	 */
	public Darknet(RandomEngine re, DataSpace ds, Settings st, EventLogger el,
			String args) {
		super(re, ds, st, el);
		setParameters();

		StringTokenizer stoke = new StringTokenizer(args,", ");

		if (stoke.countTokens() < 1) {
			throw new RuntimeException("No parameters given.");
		}
		people = new HashMap<Integer,Person>();

		String name = stoke.nextToken();
		if (name.equals("dual")) {
			if (stoke.countTokens() != 6)
				throw new RuntimeException("dual graphs: not enough arguments");
			
			int size1 = Integer.parseInt(stoke.nextToken());
			int degree1 = Integer.parseInt(stoke.nextToken());
			int size2 = Integer.parseInt(stoke.nextToken());
			int degree2 = Integer.parseInt(stoke.nextToken());
			Integer startId = Integer.parseInt(stoke.nextToken());
			int cut = Integer.parseInt(stoke.nextToken());
			people1 = swgraph(size1, 0, degree1);
			people2 = swgraph(size2, size1, degree2);
			joinNetworks(cut);
		}
		else if (name.equals("kleinberg")) {			
			if (stoke.countTokens() != 2)
				throw new RuntimeException("kleinberg: requires size and degree");
			people1 = swgraph(Integer.parseInt(stoke.nextToken()), 0, Integer.parseInt(stoke.nextToken()));
			for(int i=0; i<people1.length; i++) {
				people.put(new Integer(i), people1[i]);
			}
		}
		else if (name.equals("load")) {
			try {
				readGraph(new File(stoke.nextToken()), Integer.parseInt(stoke.nextToken()));
			} catch (IOException e) {
				throw new RuntimeException("Error reading darknet graph: " + e);
			}
		}
		else if (name.equals("loaddual")) {
			if (stoke.countTokens() != 5)
				throw new RuntimeException("loaddual requires: graph1 startid1 graph2 startid2 cut");
			try {
				people1 = readGraph2(false, new File(stoke.nextToken()), Integer.parseInt(stoke.nextToken()));
				people2 = readGraph2(false, new File(stoke.nextToken()), Integer.parseInt(stoke.nextToken()));
			} catch (IOException e) {
				throw new RuntimeException("Error reading dual darknet graphs: " + e);
			}

			int cut = Integer.parseInt(stoke.nextToken());
			joinNetworks(cut);
		} else {
			throw new RuntimeException("Darknet: options load,kleinberg,dual");
		}

		if (RANDOM_GROWTH_MODEL == 1) // All nodes become available
			for (Person p: people.values())
				ts.add(p);

		chooseTypes(people);          // Pick opennet/darknet/hybrid nodes
		System.err.println("Read graph of size " + people.size());
		System.err.println("Average degree: " + avgDeg());

		logCreation();
	}

	public float avgDeg() {
		int n = 0;
		for (Person p: people.values()) {
			n += p.degree();
		}

		return ((float) n / people.size());
	}

	/**
	 * How many nodes initiate maintenance (swapping) requests
	 */	  
	public int maintSize() {
		return n_darknet_active;
	}

	/*
	 * Partition the positions of two disjoint networks into [0,0.5] and [0.5,1]
	 * Ignoring P({pick 0.5}) > 0
	 */

	public void dualSplitPositions() {

	    if (!first_split) {
		first_split = true;
	    	    System.err.println("WARNING, modifying node locations!");
	    	    for (Person p: people1) {
		    	p.getNode().pos.r = 0.5*re.nextDouble();
	    	    }

	    	    for (Person p: people2) {
		    	p.getNode().pos.r = 0.5 + 0.5*re.nextDouble();
	    	    }
	    }
	}

	/*
	 * @return: array of the random cut between networks as (src,dst) pairs
	 */
	public Person[][] joinNetworks(int cutsize) {
	    for (int i=0; i<people1.length; i++) {
		Integer id1 = new Integer(i);
		people.put(new Integer(people1[i].id) ,people1[i]);
		people1[i].setNetwork(1);
	    }
	    for (int i=0; i<people2.length; i++) {		
		people.put(new Integer(people2[i].id), people2[i]);
		people2[i].setNetwork(2);
	    }

	    Person [][] cut = new Person [cutsize][2];
	    
	    for (int i=0; i<cutsize; i++) {
		boolean found = false;
		while (!found) {
		    Person src = people1[re.nextInt(people1.length)];
		    Person dst = people2[re.nextInt(people2.length)];
		 
		    if (!src.linksTo(dst) && !dst.linksTo(src)) {
			src.addOut(dst);
			dst.addOut(src);
			found = true;
			cut[i][0] = src;
			cut[i][1] = dst;
		    }
		}
	    }
	    return cut;
	}
	
	/*
	 * Generate a Kleinberg small-world of size, with a fixed number of links added to
	 * each node (resulting in a random number of links for nodes)
	 */
	public Person[] swgraph(int size, int offset, int outdeg) {

	       Person [] people = new Person[size];
	       for (int i=0; i<size; i++) {
		   people[i] = new Person(i+offset);
	       }

	       for (int i=0; i<size; i++) {
		   Person source = people[i];

		   for (int j=0; j<outdeg; j++) {
		       boolean found = false;
		       while (!found) {
			     double u = re.nextDouble();
			     int dist = (int) Math.floor(Math.exp(u*Math.log(size/2)));
			     int dest = i + ((re.nextDouble() < 0.5) ? -dist : dist);
			     if (dest < 0) dest = dest + size;
			     if (dest > size-1) dest = dest - size;
			     Person target = people[dest];

			     if (!source.linksTo(target) && !target.linksTo(source)) {
				source.addOut(target);
				target.addOut(source);
				found = true;
		             }
		       }
		   }
	       }
	       System.err.println("Created ideal Kleinberg graph (w.o. local connections) of size " + size);
	       return people;
	}
		
        protected void setParameters() {
		// Settings
		INITIAL_SWITCHES = st.getInt("dnInitialSwitches",100);
		MAINT_SWITCHES = st.getInt("dnMaintSwitches",100);
		RAND_STEPS = st.getInt("dnRandSteps",10);
		HTL = st.getInt("dnHTL",20);
		STORE_SIZE = st.getInt("dnStoreSize",50);
		CACHE_SIZE = st.getInt("dnCacheSize",STORE_SIZE);                 // Default: same size as store
		VARDIST_RESOLUTION = st.getInt("varDistResolution", 100);
		N_BEST = st.getInt("dnNBest",3);
		MAXPEERS = st.getInt("dnMaxPeers",20);
		RECACHE_PROB = (float) st.getDouble("dnRecacheProb",0.0);         // Whether to recache data after each successful request
		RELINK_PROB = (float) st.getDouble("dnRelinkProb",1.0);
		DARKLINKS_ONLY_PROB = (float) st.getDouble("dnLinksOnlyProb",1.0);
		if ((DARKLINKS_ONLY_PROB>1) || (DARKLINKS_ONLY_PROB<0))
		   throw new Error("DARKLINKS_ONLY_PROB (dnLinksOnlyProb) needs to be in [0,1]");

		OPENNET_NODES_SWAP_PROB = (float) st.getDouble("onSwapProb", 0.0);
		if (!(OPENNET_NODES_SWAP_PROB==0 || (OPENNET_NODES_SWAP_PROB==1)))
		   throw new Error("OPENNET_NODES_SWAP_PROB (onSwapProb) needs to be 0 or 1");

		HYBRID_NODES_SWAP_PROB = (float) st.getDouble("hnSwapProb", 0.0);
		if (!(HYBRID_NODES_SWAP_PROB==0 || HYBRID_NODES_SWAP_PROB==1))
		   throw new Error("HYBRID_NODES_SWAP_PROB (hnSwapProb) needs to be in 0 or 1");		

		RANDOM_GROWTH_MODEL = (int) st.getInt("randomGrowth",0);          // Default growth is F2F, else persons with nodes are picked at random
		if (!(RANDOM_GROWTH_MODEL==0 || RANDOM_GROWTH_MODEL==1))

		LEAVE_PERM_PROB = (float) st.getDouble("dnLeavePermProb",0.1);
		if ((LEAVE_PERM_PROB>1.0) || (LEAVE_PERM_PROB<0.0))
		    throw new Error("LEAVE_PERM_PROB (dnLeavePermProb) needs to be in [0,1]");

		// As in the freenet implementation
		OPENNET_MIN_SUCCESS_BETWEEN_DROP_CONNS = st.getInt("onMinSuccess",10); // Min successful requests between folding (Freenet 0.7)
		OPENNET_RESET_PATH_FOLDING_PROB = (float) st.getDouble("onResetFoldProb",0.05); // Probability to steal folding reference (Freenet 0.7)
		OPENNET_DROP_MIN_AGE = st.getInt("onDropMinAge",30);              // FIXME implement: Min age for node before we can drop it (Freenet 0.7)
		OPENNET_MIN_TIME_BEFORE_OFFERS = st.getInt("onMinTimeOffers",30); // FIXME implement: Min age between we give offers (Freenet 0.7)
		OPENNET_MINPEERS = st.getInt("onMinPeers",10);
		OPENNET_MAXPEERS = st.getInt("onMaxPeers",20);

		if (OPENNET_RESET_PATH_FOLDING_PROB > 1 || OPENNET_RESET_PATH_FOLDING_PROB < 0)
			throw new Error("OPENNET_RESET_PATH_FOLDING_PROB (onResetFoldProb) needs to be in [0,1]");
	}

	protected void logCreation() {
		ev.message(time(), "Created Darknet Simulator. SWITCHES: " + MAINT_SWITCHES + 
				" RANDSTEPS: " + RAND_STEPS + " HTL: " 
				+ HTL + " STORESIZE: " + STORE_SIZE + ".");
	}

	public DarknetNode newNode(int num) {

		Person p;
		
		if (RANDOM_GROWTH_MODEL==0)
			p = growNetwork();
		else
			p = growNetworkRandom();

		if (p == null)
			throw new SimulationException("Poplulation smaller than network wants to be.");
		
		DarknetNode n;
		if (!dormant.containsKey(p)) {
			double r = re.nextDouble();
			n = new DarknetNode(this, num, p, new CircleKey(r));
		} else {
			n = dormant.get(p);		    		 // Zombie
			dormant.remove(p);
		}

		return n;

	}

	public CircleKey newKey() {
		return new CircleKey(re.nextDouble());
	}

	// Not depending on oldNode (linking to a living node) at the moment... 
	public boolean join(DarknetNode newNode, DarknetNode oldNode, Feedback fb) {
		int cost = newNode.join();   		// Always connect friends

		if (newNode.isOpennet()) {		// Maybe connect strangers
		   opennetBootstrap(newNode, OPENNET_MINPEERS);
		   n_opennet_active++;
		}
		else if(newNode.isDarknet()) {
		   n_darknet_active++;
		} else {
			throw new RuntimeException("join: Node of unknown type joins network");
		}

		size++; // After bootstrap
		fb.feedback(RunTask.JOIN, true, new double[] {cost});
		return true;
	}
	
	/*
	 * Simulate a seednodes that give connections uniformly at random to a new opennet peer
	 */
	public int opennetBootstrap(DarknetNode newNode, int npeers) {

	       int found = 0;
	       for (int i = 0; i < npeers && newNode.needsOpennet(); i++) {
		   DarknetNode prospect = chooseOpennetNode();
		   if (prospect == null) {
		      if (i==0) System.err.println("Warning: an opennet node not able to bootstrap at all, " + n_opennet_active + " active opennet nodes");
		      break;  // Unavailable
		   }
		   
		   if (prospect.needsOpennet()) {
		      newNode.reLink(prospect);
		      found++;
		   }

	       }

	       if (found == 0 && newNode.needsOpennet())
		       System.err.println("Warning: an opennet node, darkdegree " + newNode.neighbors.size() + " not able to connect");
	       return found;
	}

	public void maintenance(Feedback fb) {

		int succ = 0;
		int crossing = 0;
		int crossing_succ = 0;
		CircleKey pos = null;

		for (int i = 0; i < MAINT_SWITCHES ; i++) {
			DarknetNode n = (DarknetNode) chooseNode();
			DarknetNode target = null;

			if (n != null) {
			   pos = n.pos;

			   if (n.isDarknet())
			      target = n.makeSwitch();
			   else if (n.isOpennet() && OPENNET_NODES_SWAP_PROB==1)
			      target = n.makeSwitch();
			}

			if (n!=null && target!=null) {
				if (pos == target.pos)
					succ++;

				if (n.person().network() != target.person().network()) {
					crossing++;
					if (pos == target.pos)
						crossing_succ++;
				}
			}

		}
		fb.feedback(RunTask.MAINTENANCE, true, new double[] {MAINT_SWITCHES, succ, crossing, crossing_succ});
	}


	/**
	 * Some statistics about network partitions
	 */
	public void dualstats(Feedback fb) {

	       if (people1==null || people2==null)
	       	  throw new SimulationException("Attempting dualstats() without a partition");

	       double first,second,complete;
	       first = second = 0;

	       for (int i=0; i<people1.length; i++) {
		   if (people1[i].isInNetwork())
		      first++;
	       }
	       for (int i=0; i<people2.length; i++) {
		   if (people2[i].isInNetwork())
		      second++;
	       }

	       fb.feedback(RunTask.DUALSTATS, true, new double[] {first,second,varDist(people1),varDist(people2),varDist(),varDist(people1,people2)});
	       // Total variation distance between uniform and sampling
	       // return new double[] {first,second,varDist(people1),varDist(people2),varDist(people1,people2))};
	       
	}

	/**
	 * Variation distance (for whole network)
	 */
	public double varDist() {
		int[] quant = new int[VARDIST_RESOLUTION];
		int n = 0;

		for (Person p: people.values()) {
			if (p.isInNetwork()) {
				n++;
				int i = (int) (p.getNode().pos.ident()*VARDIST_RESOLUTION);
				quant[i]++;
			}
		}

		double vd = 0;
		for (int i=0; i<VARDIST_RESOLUTION; i++) {
			double delta = Math.abs(((double)quant[i]/n) - ((double)1/VARDIST_RESOLUTION));
			vd = vd + delta;
		}

		vd = vd/2;
		if ((vd<0) || (vd>1))
			throw new SimulationException("varDist(): error computing total variation distance for whole population");

		return vd;
	}

	/**
	 * Variation distance (for distribution distance vs uniform distribution)
	 * Counting positions currently in network
	 */

	public double varDist(Person[] population) {
	    int[] quant = new int[VARDIST_RESOLUTION];
	    int n = 0;

	    for (Person p: population) {
		if (p.isInNetwork()) {
		    n++;		    
		    int i = (int) (p.getNode().pos.ident()*VARDIST_RESOLUTION);
		    quant[i]++;
		}
	    } 

	    double vd = 0;
	    for (int i=0; i<VARDIST_RESOLUTION; i++) {
	    	double delta = Math.abs(((double)quant[i]/n) - ((double)1/VARDIST_RESOLUTION));
		vd = vd + delta;
	    }

	    vd = vd/2;
	    if ((vd < 0) || (vd>1)) 
	       throw new SimulationException("varDist(): error computing total variation distance");

	    return vd;
	}

	/**
	 * Variation distance where one of the networks is seen as the reference
	 */

	public double varDist(Person[] first, Person[] second) {
		int[] quant1 = new int[VARDIST_RESOLUTION];
		int[] quant2 = new int[VARDIST_RESOLUTION];
		int n1=0,n2=0;

		for (Person p: first) {
			if (p.isInNetwork()) {
				n1++;
				int i = (int) (p.getNode().pos.ident()*VARDIST_RESOLUTION);
				quant1[i]++;
			}
		}

		for (Person p: second) {
			if (p.isInNetwork()) {
				n2++;
				int i = (int) (p.getNode().pos.ident()*VARDIST_RESOLUTION);
				quant2[i]++;
			}
		}

		double vd = 0;
		for (int i=0; i<VARDIST_RESOLUTION; i++) {
			double delta = Math.abs(((double)quant1[i]/n1) - ((double)quant2[i]/n2));
			vd = vd + delta;
		}

		vd = vd/2;
		if ((vd < 0) || (vd>1))
			throw new SimulationException("varDist(.,.): error computing mutual variation distance");

		return vd;
	}


	/*
	 * Some statistics about available keys
	 */

	public int[] keystats() {
	    int activeKeys=0,sleepingKeys=0;

	    for (Node n: nodes)
		activeKeys = activeKeys + n.nKeys();

	    for (Node n: dormant.values())
		sleepingKeys = sleepingKeys + n.nKeys();

	    return new int[] {activeKeys, sleepingKeys};
	}

	public Data search(CircleKey k, DarknetNode n, boolean ghost, Feedback fb) {
		DarknetRoute route = n.findRoute(k, null);
		Data d = route.findData(k);

		if (d != null) {
			if (RECACHE_PROB>0 && re.nextFloat()<RECACHE_PROB)
				route.storeData(d, false, true);
			else
				route.storeData(d, false, false);
		}
		
		// Freenet 0.7: path folding happens only on successful requests
		if (d != null)
			route.reLink(re, RELINK_PROB, OPENNET_RESET_PATH_FOLDING_PROB);

		int srcNet = n.person().network(), origNet = ds.getData(k).sourceNetwork();

		fb.feedback(RunTask.SEARCH, d != null, new double[] {route.size(),   d != null ? route.sizeForward() : 0,   srcNet,   origNet});
		return d;
	}

	public boolean place(Data<CircleKey> d, DarknetNode n, Feedback fb) {
		// Remember source
		d.sourceNetwork(n.person().network());
		DarknetRoute route = n.findRoute(d.key(), null);

		route.storeData(d, true, true);
		// Freenet 0.7: Inserts not leading to path folding
		// route.reLink(re, RELINK_PROB);

		fb.feedback(RunTask.PLACE, true, new double[] {route.size()});
		return true;
	}

	public void leave(DarknetNode dn, Feedback fb, boolean permanent) {
		size--;
		if (dn.isOpennet()) {
		   n_opennet_active--;
		}
		else if (dn.isDarknet()) {
		   n_darknet_active--;
		}
		else 
		   throw new RuntimeException("leave(): unknown type");

		// Put node among zombies
		if (!permanent) {
		    if (dormant.containsKey(dn.person()) || dormant.containsValue(dn))
			throw new SimulationException("Person already with dormant node, or node already dormant");
		    dormant.put(dn.person(), dn);
		}

		dn.leave(permanent);
		shrinkNetwork(dn.person());
		fb.feedback(RunTask.LEAVE, true, new double[] {1});
	}
	
	/**
	 * Does the node leave permanently, or just become dormant?
	 */
	public boolean leavePermanent(DarknetNode n) {
	    if (LEAVE_PERM_PROB == 1)
		return true;
	    if (LEAVE_PERM_PROB == 0)
		return false;
	    if (re.nextDouble() < LEAVE_PERM_PROB)
		return true;
	    else
		return false;
	}

	/*
	 *  Pick an opennet node, uniformly at random
	 */
	public DarknetNode chooseOpennetNode() {
	       if (n_opennet_active == 0)
		  return null;
	       int j = re.nextInt(n_opennet_active);
	       int k = -1;

	       for (int i=0; i < size; i++) {
		   if (nodes.get(i).isOpennet())
		      k++;
		      
		   if (k==j)
		      return nodes.get(i);
	       }

	       throw new SimulationException("chooseOpennetNode(): sampling error");
	}


	protected void readGraph(File s, int startId) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(s));
		String line;
		System.err.println("Parsing...");
		int i = 0;
		while ((line = br.readLine()) != null) {
			if (++i % 100000 == 0)
				System.err.println("Line: " + i);
			StringTokenizer st = new StringTokenizer(line, "\t");
			if (st.nextToken().equals("E")) {
				Integer id1 = new Integer(st.nextToken());
				Integer id2 = new Integer(st.nextToken());

				Person n1 = people.get(id1);
				if (n1 == null) {
					n1 = new Person(id1);
					people.put(id1, n1);
				}
				Person n2 = people.get(id2);
				if (n2 == null) {
					n2 = new Person(id2);
					people.put(id2, n2);
				}

				// There is duplicate edge protection, and we are only
				// interested
				// in undirected graphs
				n1.addOut(n2);
				n2.addOut(n1);
			}
		}

		System.err.println("Read " + people.size() + " people into the network.");

		Person p = people.get(startId);
		if (p == null)
			throw new RuntimeException("The starting identity did not exist");
		ts.add(p);
	}


	/**
	 * Reads a graph file containing the social network data.
	 * @direct: if true, then reading directly into final set of people
	 * @startId: the id in the graph that a growth process starts with
	 */
	protected Person[] readGraph2(boolean direct, File s, int startId) throws IOException {
		 BufferedReader br = new BufferedReader(new FileReader(s));
		 String line;
		 HashMap <Integer,Person> temp = new HashMap<Integer, Person>();
		 HashMap <Integer,Integer> idmap = new HashMap<Integer, Integer>();
		 Integer start = null;

		 System.err.println("Parsing...");
		 int i = 0;
		 while ((line = br.readLine()) != null) {
			 if (++i % 100000 == 0)
				 System.err.println("Line: " + i);
			 StringTokenizer st = new StringTokenizer(line, "\t");
			 if (st.nextToken().equals("E")) {
				 Integer id1raw = new Integer(st.nextToken());
				 Integer id2raw = new Integer(st.nextToken());

				 // Fix representation
				 Integer id1 = idmap.get(id1raw);
				 Integer id2 = idmap.get(id2raw);
				 if (id1 == null) {
					 id1 = new Integer(person_id_offset++);
					 idmap.put(id1raw, id1);
				 }
				 if (id2 == null) {
					 id2 = new Integer(person_id_offset++);
					 idmap.put(id2raw, id2);
				 }

				 // Node to start from
				 if ((start==null) && (startId==id1raw.intValue()))
					 start = id1;
				 if ((start==null) && (startId==id2raw.intValue()))
					 start = id2;

				 Person n1 = direct ? people.get(id1) : temp.get(id1);
				 if (n1 == null) {
					 n1 = new Person(id1);
					 if (direct)
						 people.put(id1, n1);
					 else 
						 temp.put(id1, n1);
				 }
				 Person n2 = direct ? people.get(id2) : temp.get(id2);
				 if (n2 == null) {
					 n2 = new Person(id2);
					 if (direct)
						 people.put(id2, n2);
					 else
						 temp.put(id2, n2);
				 }

				 // There is duplicate edge protection, and we are only
				 // interested in undirected graphs
				 n1.addOut(n2);
				 n2.addOut(n1);
			 }
		 }

		 System.err.println("Read " + (direct ? people.size() : temp.size()) + " people into the network.");
		 
		 if (start == null)
			 throw new RuntimeException("The starting identity did not exist");
		 Person p = direct ? people.get(start) : temp.get(start);
		 ts.add(p);

		 if (!direct) {
			 Person[] target = new Person[temp.size()];
			 int j = 0;
			 for (Person q: temp.values())
				 target[j++] = q;

			 return target;
		 }

		 return null;
	 }

	/**
	 *
	 * Distribute opennet/darknode persons over the network
	 */

	 private void chooseTypes(HashMap<Integer, Person> people) {	     	 
	 	 System.err.println("Picking darknet nodes with p = " + DARKLINKS_ONLY_PROB);		 

		 for (Person p : people.values()) {
		     if (re.nextDouble() < DARKLINKS_ONLY_PROB) {
		     	p.setDarknet();
			n_darknet_total++;
		     } else {
			p.setOpennet();
			n_opennet_total++;
		     }
		 }

		 System.err.println("Picked " + n_darknet_total + " darknet persons, and " + n_opennet_total + " opennet persons (uniformly random)");

	 }


	 /**
		 * Chooses the next person to join the network.
		 */
	 protected Person growNetwork() {
	     	 // System.err.println("TS SIZE: " + ts.size());
		 if (ts.isEmpty()) // no more people left
			 return null; 
		 Person cand = ts.last();
		 // System.err.println("Score: " + cand.score);
		 ts.remove(cand);

		 for (Iterator<Person> it = cand.neighbors() ; it.hasNext() ; ) {
			 Person p = it.next();

			 if (ts.contains(p)) {
				 ts.remove(p);
				 p.score++;		
				 ts.add(p);
			 } else {
				 p.score++;
				 if (!p.isInNetwork()) {
					 ts.add(p);
				 }
			 }
		 }

		 return cand;
	 }	 

	 /**
	   * Chooses the next person, uniformly at random rather than weighted.
	   */
	protected Person growNetworkRandom() {
	    // System.err.println("TS SIZE: " + ts.size());
		  if (ts.isEmpty())
		     return null;

		  int k = re.nextInt(ts.size());		  
		  int i = 0;
		  for (Iterator<Person> it = ts.iterator(); it.hasNext(); i++) {
		      Person p = it.next();
		      if (i==k) {
			 ts.remove(p);
			 return p;
		      }
		  }

		  throw new RuntimeException("growNetworkRandom(): sample error");
	}


	 /**
		 * Takes somebody out of the network.
		 */
	 protected void shrinkNetwork(Person p) {
		 for (Iterator<Person> it = p.neighbors() ; it.hasNext() ; ) {
			 Person n = it.next();
			 if (ts.contains(n)) {
				 ts.remove(n);
				 n.score--;		
				 ts.add(n);
			 } else {
				 n.score--;
			 }
		 }
		 ts.add(p);
	 }

}
