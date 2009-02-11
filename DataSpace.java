package simsalabim;
import java.util.*;

public interface DataSpace<K extends Key> {


    // public Data<K> newData(Simulator<?, K> god, K key);
    public Data<K> newData(Simulator god, K key);

    public Data<K> getData(K key);


    public Iterator<Data<K>> allData();

    public K chooseKey(RandomEngine re);
    
    public int size();
    
    public String info();
}
