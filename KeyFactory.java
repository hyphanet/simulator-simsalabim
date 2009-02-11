package simsalabim;
import java.util.Date;

public abstract class KeyFactory<K extends Key> {

    private static RandomEngine re = null;

    /**
	 * Creates keys for a specific architecture.
	 * 
	 * @param val
	 *            A random long (64 uniform bits).
	 */
    public abstract K newKey(long val);

    /**
	 * Creates keys for a specific architecture.
	 * 
	 * @param re
	 *            Entropy source
	 */
    public K newKey(RandomEngine re) {
        return newKey(re.nextLong());
    }

    /**
	 * Creates keys for specific architecture.
	 */
    public K newKey() {
        if (re == null)
            re = new MersenneTwister(new Date());
        return newKey(re);
    }


}
