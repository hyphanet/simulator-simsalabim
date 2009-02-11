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
	int MAX_NO_IMPROVE;
	int STORE_SIZE;
	int N_BEST;
	int RAND_STEPS;
	float RECACHE_PROB;
	float RELINK_PROB;

	HashMap<Integer, Person> people;
	TreeSet<Person> ts = new TreeSet<Person>();
	int size;

	public Darknet(RandomEngine re, DataSpace ds, EventLogger el, Settings st, File graph,
			int startId) {
		super(re, ds, st, el);

		setParameters();
		try {
			readGraph(graph, startId);
		} catch (IOException e) {
			throw new RuntimeException("Error reading darknet graph: " + e);
		}

		logCreation();
	}

	public Darknet(RandomEngine re, DataSpace ds, Settings st, EventLogger el,
			String args) {
		super(re, ds, st, el);
		setParameters();

		StringTokenizer stoke = new StringTokenizer(args,", ");

		if (stoke.countTokens() < 1) {
			throw new RuntimeException("No darknet graph given.");
		}

		try {
			readGraph(new File(stoke.nextToken()), Integer.parseInt(stoke.nextToken()));
		} catch (IOException e) {
			throw new RuntimeException("Error reading darknet graph: " + e);
		}

		System.err.println("Read graph of size " + people.size());

		logCreation();
	}
	
	private void setParameters() {
		// Settings
		INITIAL_SWITCHES = st.getInt("dnInitialSwitches",100);
		MAINT_SWITCHES = st.getInt("dnMaintSwitches",100);
		RAND_STEPS = st.getInt("dnRandSteps",5);
		MAX_NO_IMPROVE = st.getInt("dnMaxNoImprove",20);
		STORE_SIZE = st.getInt("dnStoreSize",50);
		N_BEST = st.getInt("dnNBest",3);
		RECACHE_PROB = (float) st.getDouble("dnRecacheProb",1.0);
		RELINK_PROB = (float) st.getDouble("dnRelinkProb",1.0);
	}

	private void logCreation() {
		ev.message(time(), "Created Darknet Simulator. SWITCHES: " + MAINT_SWITCHES + 
				" RANDSTEPS: " + RAND_STEPS + " MAX_NO_IMPROVE: " 
				+ MAX_NO_IMPROVE + " STORESIZE: " + STORE_SIZE + ".");
	}


	public DarknetNode newNode(int num) {

		double r = re.nextDouble();

		Person p = growNetwork();

		if (p == null)
			throw new RuntimeException("Poplulation smaller than network.");

		return new DarknetNode(this, num, p, new CircleKey(r), 0);

	}

	public CircleKey newKey() {
		return new CircleKey(re.nextDouble());
	}

	public boolean join(DarknetNode newNode, DarknetNode oldNode, Feedback fb) {
		int cost = newNode.join();
		size++;

		fb.feedback(RunTask.JOIN, true, cost);
		return true;
	}

	public void maintenance(Feedback fb) {
		for (int i = 0; i < MAINT_SWITCHES ; i++) {
			DarknetNode n = (DarknetNode) chooseNode();
			if (n != null) {
				n.makeSwitch();
			}
		}
		fb.feedback(RunTask.MAINTENANCE, true, MAINT_SWITCHES * RAND_STEPS);
	}


	public Data search(CircleKey k, DarknetNode n, boolean ghost, Feedback fb) {
		DarknetRoute route = n.findRoute(k, null, MAX_NO_IMPROVE);

		Data d = route.findData(k);

		if (d != null && re.nextFloat() < RECACHE_PROB) {
			route.sinkStore(d);
		}
		route.reLink(re, RELINK_PROB);

		fb.feedback(RunTask.SEARCH, d != null, route.size());
		return d;
	}

	public boolean place(Data<CircleKey> d, DarknetNode n, Feedback fb) {
		DarknetRoute route = n.findRoute(d.key(), null, MAX_NO_IMPROVE);
		route.sinkStore(d);
		route.reLink(re, RELINK_PROB);

		fb.feedback(RunTask.PLACE, true, route.size());
		return true;
	}

	public void leave(DarknetNode dn, Feedback fb) {
		size--;
		dn.leave();
		shrinkNetwork(dn.person());
		fb.feedback(RunTask.LEAVE, true, 1);
	}



	/**
	 * Reads a graph file containing the social network data.
	 */
	 protected void readGraph(File s, int startId) throws IOException {
		 BufferedReader br = new BufferedReader(new FileReader(s));
		 String line;
		 people = new HashMap<Integer, Person>();
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

		 Person p = people.get(startId);
		 if (p == null)
			 throw new RuntimeException("The starting identity did not exist");
		 ts.add(p);
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
