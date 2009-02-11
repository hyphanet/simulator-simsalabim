package simsalabim.utils;
import java.util.*;
/**
 * I can't believe I have to code this.
 * 
 * @author ossa
 * 
 */

public class Heap<E> {
	
	private static Comparator ncomp = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (o1 == null)
				throw new Error("o1 was null.");
			if (o2 == null)
				throw new Error("o2 was null");
			return ((Comparable)o1).compareTo((Comparable) o2);
		}
		
		public boolean equals(Object o1, Object o2) {
			return compare(o1, o2) == 0;
		}
	};
	
	public static void main(String[] args) {
		Heap<Integer> h = new Heap<Integer>();
		Random r = new Random();
		for (int i = 0 ; i < 32 ; i++) {
			h.add(r.nextInt(1000));			
		}
		
		for (int i = 0 ; i < 16 ; i++) {
			System.err.println(h.removeFirst());
		}
		
		System.err.println("Adding some more:");
		
		for (int i = 0 ; i < 16 ; i++) {
			h.add(r.nextInt(1000));
		}
		for (int i = 0 ; i < 33 ; i++) {
			System.err.println(h.removeFirst());
		}
	}
	
	
	Comparator<? super E> c;
	Vector<E> heap = new Vector<E>(); 
	private int size = 0;
	
	public Heap(Comparator<? super E> c) {
		this.c = c;
		heap.setSize(1);
	}
	
	public Heap() {
		this.c = ncomp;
		heap.setSize(1);
	}
	
	
	public E first() {
		return heap.get(1);
	}
	
	public E removeFirst() {
		E first = heap.get(1);
		fill(1);
		size--;
		return first;
	}
	
	public void add(E e) {
		replace(1,e);
		size++;
	}
	
	public int size() {
		return size;
	}
	
	/**
	 * Clears heap
	 */
	public void clear() {
		heap.clear();
		heap.setSize(1);
		size = 0;
	}
	
	/**
	 * Clears heap and sets a new comparator.
	 * 
	 * @param c
	 */
	public void setComparator(Comparator<? super E> c) {
		clear();
		this.c = c;
	}
	
	/**
	 * Fills the spot i with an element from higher in the heap (or set to null
	 * if leaf).
	 * 
	 * @param i
	 */
	private void fill(int i) {
		int i1 = i*2;
		int i2 = i*2 + 1;
		E p1 = i1 >= heap.size() ? null : heap.get(i1);
		E p2 = i2 >= heap.size() ? null : heap.get(i2);
		if (p1 == null && p2 == null) { // leaf
			heap.set(i,null);
		} else if (p2 == null || (p1 != null && c.compare(p1, p2) < 0)) {			
			heap.set(i, heap.get(i1));
			fill(i1);
		} else {
			heap.set(i, heap.get(i2));
			fill(i2);
		}
	}

	/**
	 * Add e to the tree, replacing an element at i or below
	 * 
	 * @param i
	 * @param e
	 */
	private void replace(int i, E e) {
		if (i >= heap.size()) {
			heap.setSize(i + 1);
			heap.set(i,e);
			return;
		}
		E n = heap.get(i);
		if (n == null) {
			heap.set(i,e);
			return;
		}
		int i1 = i*2;
		int i2 = i*2 + 1;
		E p1 = i1 >= heap.size() ? null : heap.get(i1);
		E p2 = i2 >= heap.size() ? null : heap.get(i2);
		
		if (c.compare(e, n) < 0) {
			heap.set(i,e);
			if (p2 == null || (p1 != null && c.compare(p1, p2) < 0)) {
				replace(i2,n);
			} else {
				replace(i1,n);
			}
		} else {
			if (p2 == null || (p1 != null && c.compare(p1, p2) < 0)) {
				replace(i1,e);
			} else {
				replace(i2,e);
			}
		}
	}
	
	/**
	 * Moves the element at spot i up to its correct position in the heap.
	 * 
	 * @param i
	 */
	private void moveUp(int i) {
		int pi = i/2;
		E n = heap.get(i);
		E p = heap.get(pi);
		if (i == 0 || c.compare(n,p) < 0) 
			return;
		else {
			heap.set(i, p);
			heap.set(pi, n);
			moveUp(pi);
		}		
	}
	
}
