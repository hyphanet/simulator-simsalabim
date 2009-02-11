package simsalabim.darknet;
import simsalabim.*;
import java.util.*;
import simsalabim.utils.*;

public class DarknetNode extends Node<Darknet, CircleKey> {

	protected LRUHash data;

	protected Person user;

	protected CircleKey pos;
	protected LinkedList<DarknetNode> neighbors;
	protected LinkedList<DarknetNode> openNeighbors;
	final int nOpen;

	private boolean inNetwork = false;

	private int numQueries = 0;

	public DarknetNode(Darknet god, int num, Person user, CircleKey pos,
			int nOpen) {
		super(god, num);
		this.user = user;

		this.pos = pos;
		this.nOpen = nOpen;

		data = new LRUHash(god.STORE_SIZE);
		neighbors = new LinkedList<DarknetNode>();
		openNeighbors = new LinkedList<DarknetNode>();
	}



	public DarknetRoute findRoute(CircleKey k, DarknetRoute dnr,
			int maxNoImprove) {

		if (!inNetwork)
			throw new Error("Node not in Network routed to: "  + this);
		if (!isActive())
			throw new Error("Node that is active routed to: " + this);

		/*
		 * This deviates in two ways from true Freenet routing. Firstly I don't
		 * backtrack, which I should, but which adds complication. Secondly
		 * Freenet doesn't have a fixed maxSteps, but stops after taking x steps
		 * without coming closer to the target value.
		 */

		if (dnr == null)
			dnr = new DarknetRoute(god.N_BEST);

		dnr.atNode(this, k);
		numQueries++;

		// terminate on success
		// if (data.contains(k))
		// return dnr;

		// find next
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

		if (bd < dnr.closest) {
			dnr.closest = bd;
			dnr.stepsSince = 0;
			// dnr.bestNode = cand;
		} else {
			dnr.stepsSince++;
			if (dnr.stepsSince > maxNoImprove)
				return dnr;
		}

		if (cand != null)
			return cand.findRoute(k, dnr, maxNoImprove);
		else 
			return dnr;

	}

	public int join() {
		inNetwork = true;
		user.joinedNetwork(this);

		for (Iterator<Person> it = user.neighbors() ; it.hasNext() ; ) {
			Person p = it.next();
			if (p.isInNetwork()) {
				makeNeighbors(p.getNode());
			}
		}

		int nsw = Math.min(god.size, god.INITIAL_SWITCHES);

		for (int i = 0 ; i < nsw ; i++) {
			makeSwitch();
		}

		return nsw * god.RAND_STEPS;

	}

	public void makeSwitch() {
		DarknetNode n = getRandomNode();
		if (n != null) {
			double mhVal = Math.min(1, Math.exp((logdist(pos) + n.logdist(n.pos) 
					- logdist(n.pos) 
					- n.logdist(pos))));

			if (god.random().nextDouble() < mhVal) {
				CircleKey mp = pos;
				pos = n.pos;
				n.pos = mp;
			} 
		}
	}

	public int leave() {
		inNetwork = false;
		user.leftNetwork();
		int n = neighbors.size();
		
		// System.err.println("DN Node leaving network.");

		for (Iterator<Person> it = user.neighbors() ; it.hasNext() ; ) {
			Person p = it.next();
			if (p.isInNetwork()) {
				removeNeighbor(p.getNode());
			}
		}
		
		if (neighbors.size() != 0)
			throw new RuntimeException("Leaving Darknetnode did not remove all neighbors.");

		for (Iterator<Data> it = data.data() ; it.hasNext() ;) {
			it.next().removedFrom(this);
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

	public Data findData(CircleKey k) {
		return data.get(k);
	}

	public void storeData(Data<CircleKey> d) {
		d.addedTo(this);
		Data r = data.put(d);
		if (r != null)
			r.removedFrom(this);
	}

	public void reLink(DarknetNode d) {
		// THIS HAS NOT BEEN IMPLEMENTED. NEED TO CONSIDER SYMMETRY!

		if (!openNeighbors.contains(d)) {
			if (openNeighbors.size() >= nOpen) 
				openNeighbors.removeFirst();
		}
	}

	/**
	 * Stores at this node, and depth generation neighbors.
	 */
	public void explosiveStore(Data d, int depth) {
		storeData(d);
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


	protected void makeNeighbors(DarknetNode dn) {
		if (!neighbors.contains(dn)) {
			neighbors.add(dn);
		}

		if (!dn.neighbors.contains(this)) {
			dn.neighbors.add(this);
		}
	}

	protected void removeNeighbor(DarknetNode dn) {
		neighbors.remove(dn);
		dn.neighbors.remove(this);
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


	public int fieldnum() {return 6;}

	public String[] fields() {
		return new String[]{"Num","Position","Degree","Queries", "Data", "LogDist"};
	}

	public String[] info() {
		return new String[] {
				Integer.toString(num), pos.stringVal(), 
				Integer.toString(neighbors.size()),
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
