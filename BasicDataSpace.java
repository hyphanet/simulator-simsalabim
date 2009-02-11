package simsalabim;
import java.util.*;

public class BasicDataSpace<K extends Key> implements DataSpace<K> {


    private HashMap<K, Data<K>> datamap = new HashMap<K, Data<K>>();
    private Vector<Data<K>> datav = new Vector<Data<K>>();

    private long minsize, sizerange;

    public BasicDataSpace(long minsize, long maxsize) {
        this.minsize = minsize;
        this.sizerange = maxsize - minsize;
    }


    public Data<K> newData(Simulator god, K key) {
        long size = minsize + (long) (god.random().nextDouble() * sizerange);
        Data<K> d = new Data<K>(god, key, size);
        datamap.put(key, d);
        datav.add(d);
        return d;
    }


    public Data<K> getData(K key) {
        return datamap.get(key);
    }

    public Iterator<Data<K>> allData() {
        return datav.iterator();
    }

    public int size() {
    	return datav.size();
    }
    
    public String info() {
    	return "BasicDataSpace size " + datav.size();
    }
    
    
    /**
	 * Currently uniform.
	 */
    public K chooseKey(RandomEngine re) {
        return datav.isEmpty() ? null : datav.get(re.choose(datav.size()) - 1).key();
    }

}
