package simsalabim.rembre;

import simsalabim.*;
import simsalabim.utils.*;

public class RembreNode<K extends PositionKey<K>> extends Node<Rembre, K> {

	private DataStore<K> datastore;
	private K pos;
	
	final RoutingTable<K, RembreNode<K>> rtTable;
	
	private int queries = 0;
	
	public RembreNode(Rembre god, K pos, int num) {
		super(god, num);
		this.pos = pos;
		this.rtTable = new RoutingTable<K, RembreNode<K>>(god.RT_SIZE, god.random());
		this.datastore = new DataStore<K>(this, god.DS_SIZE, god.DS_BYTES);
	}

	@Override
	public Data<K> findData(K k) {
		if (!isActive()) {
			throw new Error("Inactive node probed for data.");
		}
			
		return datastore.get(k);
	}

	@Override
	public void storeData(Data<K> d) {
		if (!isActive()) {
			throw new Error("Inactive node asked to store data.");
		}
		datastore.put(d);
	}

	
	void findRoute(K k, RembreRoute<K> route) {
		if (!isActive()) {
			System.err.println("Inactive node routed to.");
			return;
		} 
		queries++;
		route.atNode(this);
		if (datastore.contains(k)) {			
			route.dataFound = true;
		}
		
		// System.err.println(god.MAX_STEPS + " route.steps()" + route.steps);
		
		while(!route.dataFound && route.steps < god.MAX_STEPS && 
				route.sni < god.MAX_NO_IMPROVE) {
			// System.err.println("Route step: " + route.steps);
			RembreNode<K> next;
			do {
				next = rtTable.findRoute(k, route);
				if (next != null && !next.isActive()) {
					rtTable.remove(next.pos());
				}
			} while (next != null && !next.isActive());
			if (next == null)
				return;
			next.findRoute(k,route);
		}
	}
	
	public boolean isSink(K k) {
		return !rtTable.canRouteBetter(k, k.dist(pos));
	}

		
	public K pos() {
		return pos;
	}
	
	
	@Override
	protected void activated() {
		// tell data it is back in the network (if any exists)
		for (Data<K> d : datastore) {
			d.addedTo(this);
		}
		super.activated();
	}

	@Override
	protected void deactivated() {
		// tell data is has been removed
		for (Data<K> d : datastore) {
			d.removedFrom(this);
		}
		super.deactivated();
	}
	
	public int fieldnum() {
		return 0;
	}

	public String[] fields() {
		return new String[] {"Number","Active","Activated","Deactivated","rtSize","dsSize","dsFill","Queries"};
	}

	public String[] info() {
		return new String[] {
				Integer.toString(this.num),
				isActive() ? "1" : "0",
				Long.toString(lastActivatedTime),
				Long.toString(lastDeactivatedTime),
				Integer.toString(rtTable.size()),
				Integer.toString(datastore.size()),
				Double.toString(datastore.fill()),
				Integer.toString(queries)
		};
	}

}
