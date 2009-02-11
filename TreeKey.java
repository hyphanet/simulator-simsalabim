package simsalabim;

public class TreeKey extends PositionKey<TreeKey> {
	
	public static int BITS_USED = 12;
	
	public static KeyFactory<TreeKey> getFactory() {
		return new KeyFactory<TreeKey>() {
			public TreeKey newKey(long val) {
				return new TreeKey(val);
			}
		};
	}
	
	
	private int val;
	
	public TreeKey(long val) {
		this.val = (int) val;
	}

	@Override
	public double dist(TreeKey tk) {
		int df = tk.val ^ val;
		for (int i = 64 ; i > 0 ; i--) {
			if((df & 1) > 0)
				return i / 64.0;
			df >>= 1;
		}
		return 0.0;
	}

	public double ident() {
		return (double) val;
	}

	public String stringVal() {
		return "TK" + Long.toHexString(val);
	}
}
