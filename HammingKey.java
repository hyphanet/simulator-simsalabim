package simsalabim;

public class HammingKey extends PositionKey<HammingKey> {

	public static int BITS_USED = 12;
	
	public static KeyFactory<HammingKey> getFactory() {
		return new KeyFactory<HammingKey>() {
			public HammingKey newKey(long val) {
				return new HammingKey(val);
			}
		};
	}
	
	
	private int val;
	
	public HammingKey(long val) {
		this.val = (int) val;
	}

	@Override
	public double dist(HammingKey pk) {
		int df = pk.val ^ val;
		int bts = 0;
		for (int i = 0 ; i < BITS_USED ; i++) {
			if((df & 1) > 0)
				bts++;
			df >>= 1;
		}
		return bts / 64.0;
	}

	public double ident() {
		return (double) val;
	}

	public String stringVal() {
		return "HK" + Long.toHexString(val);
	}
	
	public static void main(String[] args) {
		java.util.Random r = new java.util.Random();
		HammingKey k1 = new HammingKey(r.nextLong());
		for (int i = 0 ; i < 10 ; i++) {
			System.err.println("----");	
			HammingKey k2 = new HammingKey(r.nextLong());
			System.err.println(toBString(k1.val));
			System.err.println(toBString(k2.val));
			System.err.println(toBString(k1.val ^ k2.val));
			System.err.println(k1.dist(k2));
		}
	}

	private static String toBString(long n) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0 ; i < 32 ; i++) {
			sb.append(n & 1);
			n >>= 1;
			// System.err.println("t" + n + "\t" + Integer.toBinaryString(n));
		}
		return sb.reverse().toString();
	}
	
}
