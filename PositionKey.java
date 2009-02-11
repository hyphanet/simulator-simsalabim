package simsalabim;
/**
 * A key which represents a position in a circular [0,1] keyspace
 * 
 * @author ossa
 * 
 */

public abstract class PositionKey<K extends PositionKey> implements Key {

	public abstract double dist(K pk);

}
