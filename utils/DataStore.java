package simsalabim.utils;
import simsalabim.*;
import java.util.*;

/**
 * Simple LRU type data storage.
 */
public class DataStore<K extends Key> implements Iterable<Data<K>>{

	public static void main(String[] args) {
		final Random r = new Random();

		Data<Key>[] d = new Data[100];
		for (int i = 0 ; i < d.length ; i++) {
			Key k = new Key() {
				double val = r.nextDouble();
				public double ident() {
					return val;
				}
				public String stringVal() {
					return Double.toString(val);
				}				
			};
			d[i] = new Data<Key>(k, 100, 0);
		}

		int[] use = new int[1000];
		for (int i = 0 ; i < use.length ; i++) {
			use[i] = r.nextInt(d.length);
		}

		DataStore<Key> ds = new DataStore<Key>(null, 100,1000);


		long time = System.currentTimeMillis();
		int count = 0;
		for (int i = 0 ; i < 1000000 ; i++) {
			ds.put(d[i % d.length]);
			count += ds.get(d[use[i % use.length]].key()) != null ? 1 : 0;
		}


		System.err.println("Took: " + (System.currentTimeMillis() - time));
		System.err.println("Got: " + (((float) count) / 1000000.0));

	}

	private static class DNode<K extends Key> {

		private static int me = 0;

		int num = ++me;
		Data<K> d;
		DNode<K> prev;
		DNode<K> next;

		public String toString() {
			return "dn " + num;
		}
	}

	private class DIterator implements Iterator<Data<K>> {
		DNode<K> curr;

		public DIterator(DNode<K> first) {
			curr = first;
		}

		public boolean hasNext() {
			return curr != null && curr.next != null;
		}

		public Data<K> next() {
			Data<K> r = curr.d;
			curr = curr.next;
			return r;
		}

		public void remove() {
			throw new Error("DataStore iterator remove not implemented");
		}
	}

	private int maxBytes;
	private int currBytes = 0;
	
	private int maxSize;
	private HashMap<K, DNode<K>> data;

	DNode<K> first;
	DNode<K> last;
	
	private Node owner;

	public DataStore(Node owner, int size, int maxBytes) {
		this.owner = owner;
		maxSize = size;
		this.maxBytes = maxBytes;
		data = new HashMap<K, DNode<K>>(size);

	}

	public void put(Data<K> d) {
		DNode<K> dn = data.get(d.key());		
		if (dn != null) { // swapdata
			if (dn.d == d) {
				bump(dn);
				return;
			}
			currBytes -= dn.d.size();
			if (owner != null)
				dn.d.removedFrom(owner);
			dn.d = d;
			bump(dn);
		} else {
			dn = new DNode<K>();
			dn.d = d;

			data.put(d.key(), dn);

			if (first == null) {
				first = dn;
				last = dn;
			} else {
				dn.next = first;
				first.prev = dn;
				first = dn;
			}
		}		
		if (owner != null)
			d.addedTo(owner);
		currBytes += d.size();


		while (data.size() > maxSize || currBytes > maxBytes) {
			// remove oldest
			data.remove(last.d.key());
			currBytes -= last.d.size();
			if (owner != null)
				last.d.removedFrom(owner);
			last = last.prev;
			if (last == null)
				first = null;
			else
				last.next = null;
		}
	}

	public Data<K> get(K k) {
		DNode<K> dn = data.get(k);
		if (dn == null)
			return null;

		bump(dn);
		return dn.d;
	}
	
	private void bump(DNode<K> dn) {
		if (dn.prev != null) {

			dn.prev.next = dn.next;

			if (dn.next == null)
				last = dn.prev;
			else
				dn.next.prev = dn.prev;


			dn.prev = null;
			first.prev = dn;
			dn.next = first;
			first = dn;
		}
	}

	public boolean contains(K k) {
		return data.containsKey(k);
	}

	public Iterator<Data<K>> iterator() {
		return new DIterator(first);
	}
	
	public int size() {
		return data.size();
	}
	
	public double fill() {
		return currBytes / (double) maxBytes;
	}
/*
 * private int count() { int count = 0; DNode<K> c = first; do {
 * //System.err.println(c); count++; c = c.next; } while (c != null); return
 * count; }
 * 
 * private int bcount() { int count = 0; DNode<K> c = last; do {
 * //System.err.println(c); count++; c = c.prev; } while (c != null); return
 * count; }
 * 
 */
}
