package simsalabim;

import java.util.*;

public abstract class Simulator<N extends Node, K extends Key> {


	protected RandomEngine re = null;

	private long time = 0;
	private int nodeNumber = 0;

	protected DataSpace<K> ds;
	protected ArrayList<N> nodes = new ArrayList<N>();
	protected EventLogger ev;
	protected Settings st;

	public Simulator(RandomEngine re, DataSpace<K> ds, Settings st, EventLogger ev) {
		this.st = st;
		this.re = re;
		this.ds = ds;
		this.ev = ev;
	}

	public void dualSplitPositions() {}

	public Simulator(DataSpace<K> ds, EventLogger ev) {
		this(new MersenneTwister(new Date()), ds, new Settings(), ev);
	}

	public long time() {
		return time;
	}
	
	public void step(long stime) {
		time += stime;
	}

	public RandomEngine random() {
		return re;
	}
	
	public EventLogger log() {
		return ev;
	}
	
	public DataSpace dataspace() {
		return ds;
	}

	// To get an active node
	public N chooseNode() {
		return nodes.isEmpty() ? null : nodes.get(re.choose(nodes.size()) - 1);
	}
	
	public K chooseKey() {
		return ds.chooseKey(re);
	}

	/**
	 * Do NOT call remove on this Iterator.
	 * 
	 * @return An iterator of all the nodes.
	 */
	public Iterator<N> allNodes() {	
		return nodes.iterator();
	}

	public int netSize() {
		return nodes.size();
	}

	public int maintSize() {
		return nodes.size();
	}
	
	public String info() {
		return "Simulator " + this.getClass().getName() + " at time " + time + " net size " +
					nodes.size() + ".";
	}

	/**
	 * Whether a node should leave permanently or stay dormant. Leave it to the network.
	 */
	public abstract boolean leavePermanent(N node);

	public void aggregateNodeData(DataAggregator da) {
		for (Iterator<N> it = nodes.iterator() ; it.hasNext() ;) {
			it.next().feedAggregator(da);
		}
	}


	/**
	 * Creates keys for specific architecture.
	 */
	public abstract K newKey();

	/**
	 * Creates a new node from a specific architecture.
	 */
	public N newNode() {
		N node = newNode(++nodeNumber);
		return node;
	}
	
	protected abstract N newNode(int num);
	
	public void addNode(N node) {
		node.setCurrentNumber(nodes.size());
		nodes.add(node);
	}
	

	public void removeNode(N n) {
		int i = n.getCurrentNumber();
		if (!nodes.get(i).equals(n)) {
			throw new SimulationException("Node not found in expected place.");
		}
		N l = nodes.remove(nodes.size() - 1);
		if (l != n) {
			l.setCurrentNumber(i);
			nodes.set(i, l);
		}
	}
	

	/**
	 * Performs a join for a node from a specific architecture.
	 * 
	 * @param newNode
	 *            The node joining.
	 * @param oldNode
	 *            A node already in the network. Null if no such node exists.
	 */
	public abstract boolean join(N newNode, N oldNode, Feedback fb);


	/**
	 * Performs a maintenance operation (which can mean anything really) on a
	 * node.
	 */
	public abstract void maintenance(Feedback fb);

	/**
	 * Runs a search query for a specific architecture.
	 */
	public Data search(K k, N n, Feedback fb) {
		return search(k, n, false, fb);
	}

	/**
	 * Gets information about the state when running with two joined networks
	 */
	public void dualstats(Feedback fb) {
	}

	/**
	 * Gets information about keys on nodes (both active and passive)
	 */
	public int[] keystats() {
	       return new int[] {-1,-1};
	}


	/**
	 * Runs a search query for a specific architecture.
	 * 
	 * @param ghost
	 *            Whether to perform the query in ghost mode. If true, there
	 *            will be no side effects on the network.
	 */
	public abstract Data<K> search(K k, N n, boolean ghost, Feedback fb);


	/**
	 * Inserts a piece of data for a specific architecture.
	 */
	public abstract boolean place(Data<K> d, N n, Feedback fb);

	/**
	 * Tells a node to leave the network. Permanently or temporarily.
	 */
	public abstract void leave(N n, Feedback fb, boolean permanent);



	public static void sleep(long millis) {
		try { Thread.sleep(millis); } catch (InterruptedException e) {}
	}

}
