
package simsalabim;
/**
 * A key which represents a position in a circular [0,1] keyspace
 * 
 * @author ossa
 * 
 */

public class CircleKey extends PositionKey<CircleKey> {

	public static KeyFactory<CircleKey> getFactory() {
		return new KeyFactory<CircleKey>() {
			public CircleKey newKey(long val) {
				val &= 0x7FFFFFFFFFFFFFFFl; // makes positive
				double dv = ((double) val) / ((double) Long.MAX_VALUE);
				return new CircleKey(dv);
			}
		};
	}
	
	
	double r;

	public CircleKey(double r) {
		this.r = r;
	}

	/**
	 * Returns a floating point value designating the nodes position in the
	 * keyspace.
	 */
	public double ident() {
		return r;
	}


	/**
	 * Key value expressed in it's natural form as a string.
	 */
	public String stringVal() {
		return "" + r;
	}



	/**
	 * It is a good idea that keys have these.
	 */
	public int hashCode() {
		long v = Double.doubleToLongBits(r);
		return (int)(v ^ (v >>> 32));
	}

	public double dist(CircleKey pk) {		
		return dist(pk.r);
	}
	
	private double dist(double p) {
		double posb = Math.max(r, p);
		double poss = Math.min(r, p);
		return Math.min(posb - poss, 1.0 - posb + poss);
	}

	public String toString() {
		return "DNK" + stringVal();
	}
	
	
	public static double calcMinPoint(CircleKey[] ck, int size) {
		double best = Double.MAX_VALUE;
		for (int i = 0 ; i < ck.length ; i++) {
			double t = ck[i].r;
			double sum = 0.0;
			for (int j = 0 ; j < ck.length ; j++) {
				double tr = ck[j].r - t;
				if (tr < 0) {
					tr += size;
				}
				sum += tr;
			}
			
			double pos = sum / ck.length + t;
			if (pos > size)
				pos -= size;
			
			double val = squareDist(pos, ck);
			if (val < best)
				best = val;
		}
		return best;
	}
	
	public static double squareDist(double x, CircleKey[] ck) {
		double sum = 0.0;
		for (int i = 0 ; i < 0 ; i++) {
			sum += ck[i].dist(x);
		}
		return Math.sqrt(sum);
	}

}
