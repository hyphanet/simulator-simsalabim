package simsalabim.utils;
import simsalabim.*;
import java.util.*;

/*
 * I implement this for the millionth time...
 */

public class LRUHash {

	public static void main(String[] args) {
		final Random r = new Random();

		Data[] d = new Data[200];
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
			d[i] = new Data(k, 100, 0);
		}

		int[] use = new int[1000];
		for (int i = 0 ; i < use.length ; i++) {
			use[i] = r.nextInt(d.length);
		}

		LRUHash ds = new LRUHash(100);


		long time = System.currentTimeMillis();
		int count = 0;
		for (int i = 0 ; i < 1000000 ; i++) {
			ds.put(d[i % d.length]);
			count += ds.get(d[use[i % use.length]].key()) != null ? 1 : 0;
		}


		System.err.println("Took: " + (System.currentTimeMillis() - time));
		System.err.println("Got: " + (((float) count) / 1000000.0));

	}

	protected HashMap<Key, DataBucket> data = new HashMap<Key, DataBucket>();

	protected DataBucket topData;
	protected DataBucket bottomData;

	protected int maxSize;

	public LRUHash(int maxSize) {
		this.maxSize = maxSize;
	}

	public Data get(Key k) {
		DataBucket db = data.get(k);
		if (db != null)
			moveUp(db);
		return db == null ? null : db.d;
	}

	public boolean contains(Key k) {
		return data.containsKey(k);
	}

	public Data put(Data d) {
		if (data.containsKey(d.key())) {
			moveUp(data.get(d.key()));
			return null;
		} else {
			DataBucket db = new DataBucket(d);	
			data.put(d.key(), db);

			addTop(db);
			return shrinkToSize();
		}
	}

	public Data remove(Key k) {
		DataBucket db = data.get(k);
		if (db != null) {
			data.remove(k);
			removeFromList(db);
			return db.d;
		} else
			return null;
	}

	public Iterator<Data> data() {
		return new DataIterator(data.values().iterator());
	}

	public int size() {
		return data.size();
	}

	private void addTop(DataBucket db) {
		db.below = topData;
		if (topData != null) 
			topData.above = db;
		topData = db;
		if (bottomData == null)
			bottomData = db;
	}

	private void removeFromList(DataBucket db) {
		if (db.above != null) {
			db.above.below = db.below;
		} else {
			topData = db.below;
		}

		if (db.below != null) {
			db.below.above = db.above;
		} else {
			bottomData = db.above;
		}

		db.above = null;
		db.below = null;
	}

	private void moveUp(DataBucket db) {

		removeFromList(db);
		addTop(db);

	}

	private Data shrinkToSize() {
		if (data.size() > maxSize) {
			DataBucket db = bottomData;
			data.remove(db.d.key());
			removeFromList(db);
			return db.d;
		} else
			return null;
	}

	private class DataBucket {
		public Data d;

		public DataBucket above;
		public DataBucket below;

		public DataBucket(Data d) {
			this.d = d;
		}
	}

	private class DataIterator implements Iterator<Data> {
		Iterator<DataBucket> it;
		public DataIterator(Iterator<DataBucket> it) {
			this.it = it;
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		public Data next() {
			return it.next().d;
		}

		public void remove() {
			it.remove();
		}
	}
}
