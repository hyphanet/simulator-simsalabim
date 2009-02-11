package simsalabim.utils;
/**
 * Interface for any class with a boolean contains() methods.
 * 
 * @author ossa
 * 
 * @param <T>
 *            The type of object it might contain.
 */
public interface Contains<T> {

	public boolean contains(T t);
}
