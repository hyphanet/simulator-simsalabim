package simsalabim;
/**
 * A key which does Kademlia style XOR distance, backed by 63 bits for a double
 * 
 * @author ossa
 * 
 */
public class XorKey extends PositionKey<XorKey>{

	public static KeyFactory<XorKey> getFactory() {
		return new KeyFactory<XorKey>() {
			public XorKey newKey(long val) {
				return new XorKey(val);
			}
		};
	}
	
	private static final long M = 0x7FFFFFFFFFFFFFFFl;
	
	private long val;
	
	/**
	 * Creates a key with value val. Will zeroe out the highest order bit.
	 * 
	 * @param val
	 */
	public XorKey(long val) {
		this.val = val & M; // make sure it is positive.
	}
	
	
	@Override
	public double dist(XorKey pk) {
		long d = pk.val ^ val;
		return ((double) d) / (double) Long.MAX_VALUE;
	}

	public double ident() {
		return (double) val;
	}

	public String stringVal() {
		return "XORK" + Long.toHexString(val);
	}

	
}
