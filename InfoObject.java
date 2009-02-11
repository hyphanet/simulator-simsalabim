package simsalabim;

public interface InfoObject {

	/**
	 * The number of fields. Must be the same for all objects of a particular
	 * type.
	 */
	public int fieldnum();

	/**
	 * The names of the fields. Must be the same for all objects of a particular
	 * type.
	 */
	public String[] fields();

	public String[] info();

}
