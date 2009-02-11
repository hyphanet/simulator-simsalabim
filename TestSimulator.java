package simsalabim;

public class TestSimulator extends Simulator {

	public TestSimulator(RandomEngine re, DataSpace ds, EventLogger ev,
			Settings st, String args) {
		super(re, ds, st, ev);
	}

	public void maintenance(Feedback fb) {
	}

	private static class TestKey implements Key {

		long val;

		public TestKey(long val) {
			this.val = val;
		}

		public double ident() {
			return ((double) val) / Long.MAX_VALUE;
		}

		public String stringVal() {
			return Long.toString(val);
		}

		public int hashCode() {
			return (int) (val ^ (val >>> 32));
		}

	}

	public Key newKey() {
		return new TestKey(re.nextLong());
	}

	private class TestNode extends Node {
		int searched = 0, placed = 0;

		public TestNode(Simulator god, int num) {
			super(god, num);
		}

		public void storeData(Data la, boolean nonvolatile) {
		}

		public Data findData(Key k) {
			return ds.getData(k);
		}

	        public int nKeys() {
		        return 0;
		}

		public int fieldnum() {
			return 3;
		}

		public String[] fields() {
			return new String[] { "Num", "Searched", "Placed" };
		}

		public String[] info() {
			return new String[] { Integer.toString(num),
					Integer.toString(searched), Integer.toString(placed) };
		}
	}

	public boolean leavePermanent(Node n) {
	        return true;
	}

	public Node newNode(int num) {
		return new TestNode(this, num);
	}

	public boolean join(Node newNode, Node oldNode, Feedback fb) {
		fb.feedback(RunTask.JOIN, true, new double[] {re.choose(20)});
		return true;
	}

	public Data search(Key k, Node n, Feedback fb) {
		return search(k, n, false, fb);
	}

	public Data search(Key k, Node n, boolean ghost, Feedback fb) {
		((TestNode) n).searched++;
		fb.feedback(RunTask.SEARCH, true, new double[] {re.choose(20)});
		return ds.getData(k);
	}

	public boolean place(Data d, Node n, Feedback fb) {
		((TestNode) n).placed++;
		fb.feedback(RunTask.PLACE, true, new double[] {re.choose(20)});
		d.addedTo(n);
		return true;
	}

	public void leave(Node n, Feedback fb, boolean permanent) {
		fb.feedback(RunTask.LEAVE, true, new double[] {re.choose(20)});
	}
}
