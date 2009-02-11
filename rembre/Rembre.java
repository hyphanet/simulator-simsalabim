package simsalabim.rembre;

import simsalabim.*;
import java.lang.reflect.*;
//import simsalabim.utils.*;
//import java.util.*;

public class Rembre<K extends PositionKey<K>> extends Simulator<RembreNode<K>, K> {
	
	int MAX_STEPS;
	int MAX_NO_IMPROVE;
	int RT_SIZE;
	int DS_SIZE;
	int DS_BYTES;
	double UPDATE_PROB;
	String KEYTYPE;
	
	private final KeyFactory<K> kf;
	
	 // This isn't threadsafe anyways, but this much we can do...
	private ThreadLocal<RembreRoute<K>> troute = new ThreadLocal<RembreRoute<K>>() {
		protected RembreRoute<K> initialValue() {
			return new RembreRoute<K>();
		}
	};

	public Rembre(RandomEngine re, DataSpace<K> ds, Settings st, EventLogger ev, String args) {
		super(re, ds, st, ev);
		setParams(st);
		try {
			Method m = Class.forName(KEYTYPE).getMethod("getFactory");
			if (m == null) {
				throw new SimulationException("rbKeyType must be a class with a " + 
							"static getFactory() method.");
			}
			kf = (KeyFactory) m.invoke(null, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new SimulationException("Exception creating Factory: " + e);
		}
	}
	
	private void setParams(Settings st) {
		MAX_STEPS = st.getInt("rbMaxSteps",100);
		MAX_NO_IMPROVE = st.getInt("rbMaxNoImprove",10);
		RT_SIZE = st.getInt("rbRoutingTableSize",25);
		DS_SIZE = st.getInt("rbDataStoreSize",100);
		DS_BYTES = st.getInt("rbDataStoreBytes",10000);
		UPDATE_PROB = st.getDouble("rbUpdateProb",0.1);
		KEYTYPE = st.get("rbKeyType","simsalabim.CircleKey");
	}

		
	@Override
	public boolean join(RembreNode<K> newNode, RembreNode<K> oldNode, Feedback fb) {
		RembreRoute<K> route = troute.get();
		K k = newNode.pos();
		route.clear(k);
		if (oldNode != null) { // all but first
			oldNode.findRoute(k, route);
			for (RembreNode<K> n : route) {
				n.rtTable.addRoute(k, newNode);
				newNode.rtTable.addRoute(n.pos(),n);
			}
			// System.err.println("Route.size(): " + route.size());
		}
		// System.err.println("Joined: oldNode " + oldNode + " route.size(): " +
		// route.size() +
		// "route.steps(): " + route.steps);
		fb.feedback(RunTask.JOIN, true, route.size());
		return true;
	}

	@Override
	public void leave(RembreNode n, Feedback fb) {
		fb.feedback(RunTask.LEAVE, true, 0);
		// nothing
	}

	@Override
	public void maintenance(Feedback fb) {
		// nothing...

	}

	@Override
	public K newKey() {
		K rk = kf.newKey(re.nextLong());
		if (rk == null)
			throw new SimulationException("Keyfactory created null key.");
		return rk;			
	}

	@Override
	public RembreNode<K> newNode(int num) {
		K pos = newKey();
		return new RembreNode<K>(this, pos, num);
	}

	@Override
	public boolean place(Data<K> d, RembreNode<K> n, Feedback fb) {
		RembreRoute<K> route = troute.get();
		route.clear(d.key()); // safety
		n.findRoute(d.key(), route);
		updateLinks(route, d.key());
		route.sinkStore(d);
		fb.feedback(RunTask.PLACE, true, route.size());
		// System.err.println("Placed route.size(): " + route.size() +
		// "route.steps(): " + route.steps);
		return true;
	}

	@Override
	public Data<K> search(K k, RembreNode<K> n, boolean ghost, Feedback fb) {
		if (k == null)
			throw new NullPointerException("Search for null key.");
		RembreRoute<K> route = troute.get();
		route.clear(k);
		n.findRoute(k, route);
		Data<K> d = route.findData(k);
		if (d != null) {
			route.sinkStore(d);
			updateLinks(route,k);
		}
		fb.feedback(RunTask.SEARCH, d != null, route.size());
		// System.err.println("Searched route.size(): " + route.size() +
		// "route.steps(): " + route.steps);

		return d;
	}
	
	private void updateLinks(RembreRoute<K> route, K k) {
		// find the closest node in the route
		RembreNode<K> best = null;
		double bd = Double.MAX_VALUE;
		for (RembreNode<K> n : route) {
			double dist = k.dist(n.pos());
			if (dist < bd) {
				bd = dist;
				best = n;
			}
		}
		for (RembreNode<K> n : route) {
			if (n != best && re.nextDouble() < UPDATE_PROB) {
				n.rtTable.addRoute(best.pos(), best);
			}
		}
	}
	
}
