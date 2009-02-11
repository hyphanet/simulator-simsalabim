package simsalabim;

/**
 * Collects data.
 */
public abstract class DataAggregator {

	private String name;

	public DataAggregator(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public void next(int val) {
		next((long) val);
	}

	public abstract void next(long val);

	public void next(float val) {
		next((double) val);
	}

	public abstract void next(double val);

	public void complete() {
	}

}
