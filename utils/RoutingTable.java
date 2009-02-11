package simsalabim.utils;
import simsalabim.*;
import java.util.*;

public class RoutingTable<K extends PositionKey, N extends Node> {
	
	private Vector<N> routes;
	private Vector<K> keys;
	private int currSize = 0;
	private int maxSize;
	private RandomEngine random;
	
	public RoutingTable(int rtSize, RandomEngine random) {
		routes = new Vector<N>(rtSize);
		keys = new Vector<K>(rtSize);
		maxSize = rtSize;
		this.random = random;
	}
	
	/**
	 * Finds the element with the closest key to k.
	 * 
	 * @param k
	 *            The key to route for.
	 * @param previous
	 *            A collection of nodes to avoid (previously tried). May be
	 *            null.
	 * @return The node in the routing table, and not in the previous
	 *         collection, or null.
	 */
	public N findRoute(K k, Contains<N> previous) {
		double best = Double.MAX_VALUE;
		N fn = null;
		for (int i = 0 ; i < currSize ; i++) {
			K key = keys.get(i);
			N node = routes.get(i);

			double d = key.dist(k);
			if (d < best && (previous != null && !previous.contains(node))) {
				fn = routes.get(i);
				best = d;
			}
		}
		return fn;
	}
	
	/**
	 * Adds a route, replacing a random old one if the table is full or if a
	 * previous route exists for the given key.
	 * 
	 * @param key
	 *            The key to associate the node with.
	 * @param node
	 *            The node to add to the routing table.
	 * @return A node which was replaced.
	 */
	public N addRoute(K key, N node) {
		int pndx = keys.indexOf(key);
		if (pndx != -1) {
			N n = routes.get(pndx);
			routes.set(pndx, node);
			return n;
		} else if (currSize < maxSize) {
			routes.add(currSize,node);
			keys.add(currSize, key);
			currSize++;
			return null;
		} else {
			int ndx = random.nextInt(currSize);
			N n = routes.get(ndx);
			routes.set(ndx, node);
			keys.set(ndx,key);
			return n;
		}
	}
	
	/**
	 * Removes an element.
	 * 
	 * @param k
	 *            The key of the element to remove.
	 * @return The element that was removed, or null if no element matching k
	 *         was found.
	 */
	public N remove(K k) {
		int ndx = keys.indexOf(k);
		if (ndx != -1) {
			currSize--;
			N n = routes.get(ndx);
			keys.set(ndx, keys.get(currSize));
			routes.set(ndx, routes.get(currSize));
			keys.remove(currSize);
			routes.remove(currSize);
			return n;
		} else
			return null;		
	}
	
	public int size() {
		return routes.size();
	}
	
	public boolean canRouteBetter(K k, double dist) {
		for (int i = 0 ; i < currSize ; i++) {
			if (k.dist(keys.get(i)) < dist) {
				return true;
			}
		}
		return false;
	}
	
}
