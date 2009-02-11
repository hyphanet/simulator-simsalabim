package simsalabim.rembre;
import simsalabim.*;
import simsalabim.utils.Contains;
import simsalabim.utils.Heap;
import java.util.*;

public class RembreRoute<K extends PositionKey<K>> implements Contains<RembreNode<K>>, Iterable<RembreNode<K>> {

	KeyComparator<K> kc = new KeyComparator<K>();
	
	Collection<RembreNode<K>> route = new HashSet<RembreNode<K>>();
	Heap<RembreNode<K>> nh = new Heap<RembreNode<K>>(kc);
	
	double closest = Double.MAX_VALUE;
	int sni = 0;
	int steps= 0;
	K target;
	boolean dataFound = false;
	
	public void atNode(RembreNode<K> dn) {
		route.add(dn);
		steps++;
		sni++;
		if (target == null)
			throw new NullPointerException("Target is null.");
		double dist = target.dist(dn.pos());
		if (dist < closest) {
			// System.err.println("New best: " + dist + " < " + closest);
			sni = 0;
			closest = dist; 
		}
	}
	
	public void clear(K target) {
		this.dataFound = false;
		this.target = target;
		route.clear();
		kc.setTarget(target);
		nh.setComparator(kc);
		closest = Double.MAX_VALUE;
		sni = 0;
		steps = 0;
	}

	public int steps() {
		return steps;
	}
	
	public int size() {
		return route.size();
	}

	public Iterator<RembreNode<K>> iterator() {
		return route.iterator();
	}

	public boolean contains(RembreNode dn) {
		return route.contains(dn);
	}

	public Data<K> findData(K k) {
		for (Iterator<RembreNode<K>> it = route.iterator() ; it.hasNext() ;) {
			Data<K> d  = it.next().findData(k);
			if (d != null)
				return d;
		}
		return null;
	}

	public void storeData(Data<K> d) {
		for (Iterator<RembreNode<K>> it = route.iterator() ; it.hasNext() ;) {
			it.next().storeData(d);
		}
	}

	public void sinkStore(Data<K> d) {
		for (RembreNode<K> n : route) {
			if (n.isSink(d.key())) {
				n.storeData(d);
			}
		}
	}
	
	
	
	private static class KeyComparator<K extends PositionKey<K>> implements Comparator<RembreNode<K>> {
		
		private K target;
		
		public void setTarget(K target) {
			this.target = target;
		}
		
		public int compare(RembreNode<K> n1, RembreNode<K> n2) {
			double d = target.dist(n1.pos()) - target.dist(n2.pos());
			return d < 0 ? -1 : d == 0 ? 0 : 1; 
		}
		
	}

}

