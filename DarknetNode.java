package simsalabim;
import java.util.*;
import simsalabim.utils.*;

public class DarknetNode extends Node<Darknet, CircleKey> {

	protected LRUHash data;	
	protected LRUHash cache;
	protected Person user;

	protected CircleKey pos;
	protected LinkedList<DarknetNode> neighbors;
	protected HashMap<DarknetNode,Boolean> neighbors_lookup;
	protected HashMap<DarknetNode,Boolean> open_lookup;
	protected LinkedList<DarknetNode> openNeighbors;

	private boolean inNetwork = false;

	private int numQueries = 0;

	public DarknetNode(Darknet god, int num, Person user, CircleKey pos) {
		super(god, num);
		this.user = user;

		this.pos = pos;

		data = new LRUHash(god.STORE_SIZE);
		cache = new LRUHash(god.CACHE_SIZE);
		neighbors = new LinkedList<DarknetNode>();
		openNeighbors = new LinkedList<DarknetNode>();
		neighbors_lookup = new HashMap<DarknetNode,Boolean>();
		open_lookup = new HashMap<DarknetNode,Boolean>();
	}

	public boolean isOpennet() {
	    return user.isOpennet();
	}

	public boolean isDarknet() {
	    return user.isDarknet();
	}

	/** 
	 * Need of opennet: always except when all slots filled by darknet peers
	 */
	public boolean needsOpennet() {
	    if (!isOpennet())
	       throw new SimulationException("DarknetNode asked about opennet refs without being an opennet node");
	    return neighbors.size() < Math.min(god.OPENNET_MAXPEERS, god.MAXPEERS);
	}

	public boolean needsDarknet() {
		return neighbors.size() < god.MAXPEERS;
	}

	public DarknetRoute findRoute(CircleKey k, DarknetRoute dnr) {

		if (!inNetwork)
			throw new Error("Node not in Network routed to: "  + this);
		if (!isActive())
			throw new Error("Node that is active routed to: " + this);

		/*
		 * Routing with
		 * 1) Backtracking
		 * 2) HTL (fixed decrease per node, not probabilistic as in freenet)
		 *
		 * Cache/Store: when route has completed
		 */

		if (dnr == null)
			dnr = new DarknetRoute(god.N_BEST, god.HTL);

		dnr.atNode(this, k);
		numQueries++;

		if (data.contains(k) || cache.contains(k)) 		// terminate on success
			return dnr;

		while (dnr.htl > 0) {
		      double bd = Double.MAX_VALUE;
		      DarknetNode cand = null;

		      for (Iterator<DarknetNode> it = neighbors() ; it.hasNext() ;) {
		      	  DarknetNode t = it.next();
			  if (dnr.contains(t))
			     continue;

			  double cd = t.pos.dist(k);

			  if (cd < bd) {
			     bd = cd;
			     cand = t;
			  }
		      }

		      if (cand != null) {
			  DarknetRoute branch = cand.findRoute(k, dnr);
			  dnr.atNode(this,k);

			  if (branch.findData(k) != null) {
			      if (hasOpennetNeighbor(cand))
				      moveUpOpennet(cand);             // Freenet 0.7: Keep opennet nodes at LRU

			      return dnr;
			  }
		      } else {
			  return dnr; 		// Dead end
		      }
		}

		return dnr;
		
	}

	/**
	 * To keep opennet peers sorted in most-recently used
	 */
	public void moveUpOpennet(DarknetNode on) {
		if (!hasOpennetNeighbor(on))
			throw new SimulationException("moveUpOpennet(): Lacking neighbor");

		openNeighbors.remove(on);
		openNeighbors.addFirst(on);		
	}

	public int nKeys() { return data.size(); }

	/**
	 *  Connect to online darknet nodes up to a limit. With many available, pick at random.
	 */
	public void refreshDarknet() {
		int room = god.MAXPEERS - neighbors.size();
		if (room == 0)
			return;
		if (room < 0)
			throw new SimulationException("refreshDarknet(): exceeding MAXPEERS");

		int avail = 0;
		for (Iterator<Person> it = user.neighbors() ; it.hasNext() ; ) {
			Person p = it.next();
			if (p.isInNetwork() && !hasDarknetNeighbor(p.getNode()) && p.getNode().needsDarknet())
				avail++;
		}
		if (avail == 0)
			return;

		if (avail <= room) {
			for (Iterator<Person> it = user.neighbors() ; it.hasNext() ; ) {
				Person p = it.next();
				if (p.isInNetwork() && !hasDarknetNeighbor(p.getNode()) && p.getNode().needsDarknet()) {
					makeNeighbors(p.getNode());
				}
			}
		} 
		else {
			// If more peers available than room, pick randomly
			for (int i=0; i<room; i++) {
				int ni = god.random().nextInt(avail-i);
				int nj = 0;
				Person p = null;

				for (Iterator<Person> it = user.neighbors(); it.hasNext() ; ) {
					p = it.next();
					if (p.isInNetwork() && !hasDarknetNeighbor(p.getNode()) && p.getNode().needsDarknet()) {
						if (nj == ni)
							break;
						nj++;
					}
				}

				if (ni != nj) throw new SimulationException("Sample error: picking darknet peer");

				makeNeighbors(p.getNode());
			}
		}
	}

	/**
	 * Drop opennet nodes if we have too many.
	 * Drop policy: LRU wrt. successful requests
	 */

	public void policyOpennet() {
		if (!isOpennet()) 
			throw new SimulationException("Non-opennet node imposing opennet policy");

		int maxopen = Math.min(god.OPENNET_MAXPEERS, god.MAXPEERS);
		if (openNeighbors.size() > Math.max(0, maxopen-neighbors.size())) {
			int drop = openNeighbors.size() - Math.max(0, maxopen-neighbors.size());

			for (int i=0; i<drop; i++) {
				DarknetNode on = openNeighbors.getLast();  // Freenet 0.7: LRU
				removeOpenNeighbor(on);
			}
		}
	}

	public int join() {
		inNetwork = true;
		user.joinedNetwork(this);
		refreshDarknet();

		if (this.isDarknet()) {
			int nsw = Math.min(god.size, god.INITIAL_SWITCHES);
		   	for (int i = 0 ; i < nsw ; i++) {
			    makeSwitch();
			}
			return nsw * god.RAND_STEPS;
		}

		return 0;
	}

	public DarknetNode makeSwitch() {

	        if (!this.isDarknet() && !(god.HYBRID_NODES_SWAP_PROB==1 || god.OPENNET_NODES_SWAP_PROB==1)) {
		   throw new Error("makeSwitch() forbidden for non-darknet nodes");
		}

		DarknetNode n = getRandomNode();
		if ((n != null) && (n.isDarknet() || god.HYBRID_NODES_SWAP_PROB==1 || god.OPENNET_NODES_SWAP_PROB==1)) {
			double mhVal = Math.min(1, Math.exp((logdist(pos) + n.logdist(n.pos) 
					- logdist(n.pos) 
					- n.logdist(pos))));

			if (god.random().nextDouble() < mhVal) {
				CircleKey mp = pos;
				pos = n.pos;
				n.pos = mp;
			} 
		}
		
		return n;

	}

	public int leave(boolean permanent) {
		inNetwork = false; //NB: person disappears before node, to avoid peers reconnecting
		user.leftNetwork();
		int n = neighbors.size() + openNeighbors.size();

		for (Iterator<Person> it = user.neighbors() ; it.hasNext() ; ) {
			Person p = it.next();
			if (p.isInNetwork()) {
				removeNeighbor(p.getNode());
				p.getNode().refreshDarknet();
			}
		}
		
		for (Iterator<DarknetNode> it = openNeighbors.iterator() ; it.hasNext() ; ) {
		    DarknetNode open = it.next();
		    open.openNeighbors.remove(this);
		}
		openNeighbors.clear();

		// Dormant nodes dont remove data
		if (permanent) {
		    for (Iterator<Data> it = data.data() ; it.hasNext() ;) {
			it.next().removedFrom(this);
		    }

		    for (Iterator<Data> it = cache.data() ; it.hasNext() ;) {
			it.next().removedFrom(this);
		    }
		}

		return n;
	}

	public boolean isSink(CircleKey k) {
		for (DarknetNode n : neighbors) {
			if (k.dist(n.pos) < k.dist(pos))
				return false;
		}
		return true;
	}

	public boolean hasData(CircleKey k) {
		return data.contains(k) || cache.contains(k);
	}

	public Data findData(CircleKey k) {
		Data cached = cache.get(k);

		return (cached != null) ? cached : data.get(k);
	}

	/**
	 * Always store in cache
	 * @sink: Whether to store in the long-term storage
	 */
	  
	public void storeData(Data<CircleKey> d, boolean sink) {
	    if (sink) {
		if (!data.contains(d.key()))
		    d.addedTo(this);
		Data r = data.put(d);
		if (r != null)
			r.removedFrom(this);
	    }

	    if (!cache.contains(d.key()))
		d.addedTo(this);
	    Data r = cache.put(d);
	    if (r != null)
		r.removedFrom(this);

	}

	/**
	 * Stores at this node, and depth generation neighbors.
	 */
	public void explosiveStore(Data d, int depth) {
	        storeData(d, true); // Default option to put into store/cache
		if (depth > 0) {
			for (Iterator<DarknetNode> it = neighbors.iterator() ; it.hasNext() ;) {
				it.next().explosiveStore(d, depth - 1);
			}
		}
	}


	public Iterator<DarknetNode> neighbors() {
		return new DoubleIterator<DarknetNode>(neighbors.iterator(), 
				openNeighbors.iterator());
	}

	public Person person() {
		return user;
	}

	// Get random node in the network (to later perform swap attempt)
	protected DarknetNode getRandomNode() {
		if (neighbors.size() == 0)
			return null; // this; using null to cause crash because this is
							// bad.

		DarknetNode curr = this;
		for (int i = 0 ; i < god.RAND_STEPS ; i++) {
			curr = curr.neighbors.get(god.random().nextInt(curr.neighbors.size()));
		}
		return curr;
	}

	public boolean hasDarknetNeighbor(DarknetNode dn) {
		return neighbors_lookup.containsKey(dn);		
	}

	public boolean hasOpennetNeighbor(DarknetNode on) {
		return open_lookup.containsKey(on);
	}

	/**
	 * Adding darknet peers may result in dropping opennet peers
	 */
	protected void makeNeighbors(DarknetNode dn) {

		if (this.needsDarknet() && dn.needsDarknet()) {
			if (!hasDarknetNeighbor(dn)) {
				neighbors.add(dn);
				neighbors_lookup.put(dn, null);
				if (isOpennet())
					policyOpennet();
			}

			if (!dn.hasDarknetNeighbor(this)) {
				dn.neighbors.add(this);
				dn.neighbors_lookup.put(this, null);
				if (dn.isOpennet())
					dn.policyOpennet();
			}
		}
	}

	/**
	 * Add opennet connection with imposing policy
	 */
	public void reLink(DarknetNode on) {
		if (!hasOpennetNeighbor(on) && !hasDarknetNeighbor(on)) {
			makeOpenNeighbors(on);
			policyOpennet();
			on.policyOpennet();
		}
	}

	protected void makeOpenNeighbors(DarknetNode on) {
		if (!hasDarknetNeighbor(on)) {    		// Protection against dual darknet/opennet peers
			if (!hasOpennetNeighbor(on)) {
				openNeighbors.add(on);
				open_lookup.put(on, null);
			}
			
			if (!on.hasOpennetNeighbor(this)) {
				on.openNeighbors.add(this);
				on.open_lookup.put(this, null);
			}
		}
	}

	protected void removeNeighbor(DarknetNode dn) {
		neighbors.remove(dn);
		neighbors_lookup.remove(dn);
		dn.neighbors.remove(this);
		dn.neighbors_lookup.remove(this);
	}

	protected void removeOpenNeighbor(DarknetNode on) {
		openNeighbors.remove(on);
		open_lookup.remove(on);
		on.openNeighbors.remove(this);
		on.open_lookup.remove(this);
	}

	/**
	 * Calculates the log distance to the neighbors of this node from newpos. If
	 * a neighbor has position newpos, then it is given my current position.
	 */
	private double logdist(CircleKey newpos) {
		double val = 0.0f;
		for (Iterator<DarknetNode> it = neighbors.iterator() ; it.hasNext() ;) {
			DarknetNode dn = it.next();
			val += Math.log(dn.pos == newpos ? pos.dist(newpos) : 
				dn.pos.dist(newpos));
		}
		return val;
	} 


	public int fieldnum() {return 7;}

	public String[] fields() {
		return new String[]{"Num","Position","DarkDegree","OpenDegree","Queries", "Data", "LogDist"};
	}

	public String[] info() {
		return new String[] {
				Integer.toString(num), pos.stringVal(), 
				Integer.toString(neighbors.size()), Integer.toString(openNeighbors.size()),
				Integer.toString(numQueries), Integer.toString(data.size()),
				Double.toString(logdist(pos))
		};
	}

	public String toString() {
		return "DN#" + num + " (" + pos + ")";
	}


	/**
	 * Feeds an aggregator called "LinkDistances" with the keyspace distance to
	 * each of my long links. Feeds an aggregator called "NodeTraffic" with the
	 * number of queries that passed me.
	 */
	public void feedAggregator(DataAggregator da) {
		super.feedAggregator(da);

		if (da.name().equals("LinkDistances")) {
			for (DarknetNode n : neighbors)
				da.next(pos.dist(n.pos));
		} else if (da.name().equals("NodeTraffic")) {
			da.next(numQueries);
		} 
	}



	private class DoubleIterator<T> implements Iterator<T> {

		private Iterator<T> first;
		private Iterator<T> second;
		private boolean f;

		public DoubleIterator(Iterator<T> first, Iterator<T> second) {
			this.first = first;
			this.second = second;

			f = first.hasNext();		
		}

		public T next() {
			if (f) {
				T r = first.next();
				f = first.hasNext();
				return r;
			} else {
				return second.next();
			}
		}

		public boolean hasNext() {
			return f || second.hasNext();
		}

		public void remove() {
			if (f)
				first.remove();
			else
				second.remove();
		}

	}

}
