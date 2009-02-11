package simsalabim;

/**
 * A note about keys: Keys are assumed to be identified by reference. Do not use
 * two objects for the same key, much code will treat them as different!
 */
public interface Key {

	/**
	 * Returns a floating point value designating the nodes position in the
	 * keyspace.
	 */
	public double ident();

	/**
	 * Key value expressed in it's natural form as a string.
	 */
	public String stringVal();

	/**
	 * It is a good idea that keys have these.
	 */
	public int hashCode();

}
