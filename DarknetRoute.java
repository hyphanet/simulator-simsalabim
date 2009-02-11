// The representation of a route
// 
// A route is represented by all the nodes passed from source to
// destination, and then the sequence of nodes returning to source. 
// This means that backtracking may be an explicit part of a route to the destination,
// as well as the path from the target back to the source.
//
// Most operations here (such as storage) assume a successful route to work correctly

package simsalabim;
import simsalabim.utils.*;
import java.util.*;


public class DarknetRoute implements Iterable<DarknetNode>, Contains<DarknetNode> {

	LinkedList<DarknetNode> route = new LinkedList<DarknetNode>();
	HashMap<DarknetNode,Boolean> visited = new HashMap<DarknetNode,Boolean>(); // Space-Time tradeoff for DarknetNode routing

	double closest = Double.MAX_VALUE;
	int stepsSince = 0; // Obsolete
	int htl = 0;

	DarknetNode bestNodes[];
	
	private int nBest;
	
	public DarknetRoute(int nBest, int htl) {
		this.nBest = nBest;
		this.htl = htl;
		bestNodes = new DarknetNode[nBest];
	}
	
	public void atNode(DarknetNode dn, CircleKey k) {
	        route.add(dn);
		visited.put(dn,null);
	    	htl--;             	   // Another hop for this route

		int n = -1;
		double md = 0;
		for (int i = 0 ; i < nBest ; i++) {
			if (bestNodes[i] == null) {
				n = i;
				break;
			}
			double cd = k.dist(bestNodes[i].pos);
			if (cd > md) {
				n = i;
				md = cd;
			}
		}

		if (n >= 0 && (bestNodes[n] == null ||  
				k.dist(bestNodes[n].pos) > k.dist(dn.pos))) {
			bestNodes[n] = dn;
		}

	}


	public int size() {
		return route.size();
	}

	public Iterator<DarknetNode> iterator() {
		return route.iterator();
	}

	public boolean contains(DarknetNode dn) {
		// return route.contains(dn);
		return visited.containsKey(dn);
	}

	public Data findData(CircleKey k) {
		for (Iterator<DarknetNode> it = route.iterator() ; it.hasNext() ;) {
			Data d  = it.next().findData(k);
			if (d != null)
				return d;
		}
		return null;
	}

	//public void storeData(Data d) {
	//	for (Iterator<DarknetNode> it = route.iterator() ; it.hasNext() ;) {
	//		it.next().storeData(d);
	//	}
	//}

	/**
	 * Store data in the route (always in cache, maybe also permanent)
	 *
	 * @d: data
	 * @sink: to attempt storage in sink
	 * @full: to store in the full (forward) path, else only in the return path
	 */
	
	public int storeData(Data<CircleKey> d, boolean sink, boolean full) {

		int nsink = 0;

	        LinkedList<DarknetNode> target;
		if (full)
		   target = route;
		else
		   target = retpath();

		for (DarknetNode n : target) {
			if (sink && n.isSink(d.key())) {
				n.storeData(d, true);
				nsink++;
			}
			else
				n.storeData(d, false);
		}

		return nsink;
	}

	//public void storeBest(Data d) {
	//	for (int i = 0 ; i < nBest ; i++) {
	//		if (bestNodes[i] != null)
	//			bestNodes[i].storeData(d);
	//	}
	//}

	/**
	 * ASSUMES: a successful query route (source ... dest ... source)
	 *
	 * Returns the return path (dest to source)
	 */

	public LinkedList<DarknetNode> retpath() {
	    LinkedList<DarknetNode> retpath = new LinkedList<DarknetNode>();

	    /* Obtain the return path */
	    ListIterator<DarknetNode> li = route.listIterator(route.size());
	    while (li.hasPrevious()) {
		DarknetNode n = li.previous();
		if (retpath.contains(n))
		    break;
		retpath.addFirst(n);
	    }
	    return retpath;
	}


	/**
	 * Relinks nodes open neighbors to one of the best nodes with given
	 * probability, if possible.
	 * ASSUMES: that this DarknetRoute contains WHOLE PATH (source ... dest ... source)
	 */
	public void reLink(RandomEngine re, float prob, float rewrite_prob) {
	        LinkedList<DarknetNode> retpath = retpath();

		// Each node, when it wants to, may add its own ref to a back-propagating request

		DarknetNode target = (retpath.getFirst().isOpennet() && retpath.getFirst().needsOpennet()) ? retpath.getFirst() : null;
		ListIterator<DarknetNode> retitr = retpath.listIterator(1);
		while (retitr.hasNext()) {
		      DarknetNode current = retitr.next();
		      if (!current.isOpennet() || !current.needsOpennet())
			 continue;

		      if (target == null) {
			      target = current;         // Always seek connections
			      continue;
		      }
		      else if (re.nextFloat() < rewrite_prob) { // Maybe steal the reference (as in Freenet 0.7)
			      target = current;
		      }
		      else if (re.nextFloat() < prob) {
			 current.reLink(target);

			 // Maybe propagate ref backwards
			 if (current.needsOpennet()) {
			    target = current;
			 } else {
			    target = null;
			 }

		      }
		}
	}
}
