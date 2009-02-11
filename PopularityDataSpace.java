package simsalabim;

import java.util.*;

public class PopularityDataSpace<K extends Key> implements DataSpace<K> {

	private ArrayList<Data<K>> data;		
	private HashMap<K, Data<K>> datamap;
	private double popDecay;
	private int currSize;
	private int maxSize;
	
	private int minDataSize;
	private int dataSizeRange;
	
	public PopularityDataSpace(int size, double popDecay, int minSize, int maxSize) {
		data = new ArrayList<Data<K>>(size);
		datamap = new HashMap<K, Data<K>>();
		this.popDecay = popDecay;
		this.currSize = 0;
		this.maxSize = size;
		this.minDataSize = minSize;
		this.dataSizeRange = maxSize - minSize;
	}
	
	public Iterator<Data<K>> allData() {
		return data.iterator();
	}

	public K chooseKey(RandomEngine re) {
		return currSize == 0 ? null : data.get(getIndex(re)).key();
	}

	public Data<K> getData(K key) {
		return datamap.get(key);
	}
	
	public String info() {
		return "PopularityDataSpace docs: " + data.size() + " (of " + maxSize + ")"; 
	}

	/**
	 * Keep min(currSize,maxSize) documents available for search
	 */

	public Data<K> newData(Simulator god, K key) {
		long datasize = minDataSize + (long) (god.random().nextDouble() * dataSizeRange);
		Data<K> d = new Data<K>(god, key, datasize);
		
		if (currSize < maxSize) {
			data.add(d);
			datamap.put(key,d);
			currSize++;
			return d;
		} else {
			int idx = god.re.nextInt(maxSize);
			moveDown(idx, god.re,god.time());
			data.set(idx,d);
			datamap.put(key, d);
			return d;
		}
	}

	public int size() {
		return currSize;
	}
	
	/**
	 * Sample from the specified popularity (Zipf) distribution
	 */
	 
	private int getIndex(RandomEngine re) {

		// Uniform
		if (popDecay == 0)
			return re.nextInt(currSize);

		// Easy distribution
		if (popDecay > 0.99 && popDecay < 1.01)
			return (int) Math.pow(currSize + 1, re.nextDouble()) - 1;

		// General case			
		double zsum = 0;
		for (int i=1; i < currSize; i++) {
			zsum += (1/Math.pow(i,popDecay));
		}
		double r = re.nextDouble();
		double z = 0;
		int x = 0;
		
		while (r > (z/zsum)) {
			z += 1/Math.pow(++x,popDecay);
		}	    
		
		return x;
	}
	
	private void moveDown(int n, RandomEngine re, long time) {
		moveDown(n, re.nextInt(maxSize) + 1, data.get(n), time);
	}
	
	private void moveDown(int n, int m, Data<K> d, long time) {
		if ((n + m) >= maxSize) {
			datamap.remove(d.key());
			d.forgotten(time);			
		} else {
			Data<K> md = data.get(n + m);
			data.set(n+m,d);
			moveDown(n+m,2*m, md, time);
		}
	}

}
