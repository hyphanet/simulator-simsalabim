package simsalabim;
import java.util.*;

public class LimitedDataSpace<K extends Key> implements DataSpace<K> {


	private HashMap<K, Data<K>> datamap;
	private Vector<Data<K>> datav;
	private Vector<Data<K>> forgotten;

	private long minsize, sizerange;
	private long maxdocs;
	
	private long currSize = 0;

	public LimitedDataSpace(int maxdocs, long minsize, long maxsize) {
		this.minsize = minsize;
		this.sizerange = maxsize - minsize;
		this.maxdocs = maxdocs;
		
		datamap = new HashMap<K, Data<K>>();
		datav = new Vector<Data<K>>(maxdocs);	
		forgotten = new Vector<Data<K>>(maxdocs);
	}


	public Data<K> newData(Simulator god, K key) {
		long size = minsize + (long) (god.random().nextDouble() * sizerange);
		Data<K> d = new Data<K>(god, key, size);
		datamap.put(key, d);
		currSize += d.size();
		
		if (datav.size() < maxdocs) {
			datav.add(d);
		} else {
			int n = god.random().nextInt(datav.size());
			Data<K> rm = datav.get(n);
			rm.forgotten(god.time());
			forgotten.add(rm);
			datamap.remove(rm.key());
			currSize -= rm.size();
			datav.set(n, d);
		}

		return d;
	}


	public Data<K> getData(K key) {
		return datamap.get(key);
	}

	public Iterator<Data<K>> allData() {
		return datamap.values().iterator();
	}

	public int size() {
		return datav.size();
	}
	
	public String info() {
		return "LimitedDataSpace docs: " + datav.size() + " (of " + maxdocs + ") bytes: " + currSize; 
	}
	
	/**
	 * Currently uniform.
	 */
	public K chooseKey(RandomEngine re) {
		if (datav.isEmpty())
			return null;
		return datav.get(re.nextInt(datav.size())).key();
	}

}
