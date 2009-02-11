package simsalabim;
import simsalabim.*;
import simsalabim.utils.*;
import java.util.*;


public class DarknetRoute implements Iterable<DarknetNode>, Contains<DarknetNode> {

	LinkedList<DarknetNode> route = new LinkedList<DarknetNode>();

	double closest = Double.MAX_VALUE;
	int stepsSince = 0;

	DarknetNode bestNodes[];
	
	private int nBest;
	
	public DarknetRoute(int nBest) {
		this.nBest = nBest;
		bestNodes = new DarknetNode[nBest];
	}
	
	public void atNode(DarknetNode dn, CircleKey k) {
		route.add(dn);

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
		return route.contains(dn);
	}

	public Data findData(CircleKey k) {
		for (Iterator<DarknetNode> it = route.iterator() ; it.hasNext() ;) {
			Data d  = it.next().findData(k);
			if (d != null)
				return d;
		}
		return null;
	}

	public void storeData(Data d) {
		for (Iterator<DarknetNode> it = route.iterator() ; it.hasNext() ;) {
			it.next().storeData(d);
		}
	}

	public void sinkStore(Data<CircleKey> d) {
		for (DarknetNode n : route) {
			if (n.isSink(d.key())) {
				n.storeData(d);
			}
		}
	}

	public void storeBest(Data d) {
		for (int i = 0 ; i < nBest ; i++) {
			if (bestNodes[i] != null)
				bestNodes[i].storeData(d);
		}
	}


	/**
	 * Relinks nodes open neighbors to one of the best nodes with given
	 * probability, if possible.
	 */
	public void reLink(RandomEngine re, float prob) {
		for (DarknetNode n : route) {
			if (n.nOpen > 0 && re.nextFloat() < prob) {
				// THIS IS NOT THIS EASY. I NEED TO FIND AN OPEN BEST NODE!
				n.reLink(bestNodes[re.nextInt(nBest)]);
			}
		}
	}

}
