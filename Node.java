package simsalabim;


public abstract class Node<S extends Simulator, K extends Key> implements InfoObject {

    protected final S god;
    protected final int num;
    
    private boolean active;    
    private int currentNumber;
    
    protected long lastActivatedTime = -1;
    protected long lastDeactivatedTime = -1;
    
    public Node(S god, int num) {
        this.god = god;
        this.num = num;
    }

    public int num() {
        return num;
    }

    /**
	 * Returns the simulator to which this node belongs.
	 */
    public S sim() {
        return god;
    }

    /**
	 * Returns the time from this nodes simulator.
	 */
    public long time() {
        return god.time();
    }


    public String toString() {
        return "N/#" + num + "/";
    }

    public void activate() {
    	active = true;
    	lastActivatedTime = god.time();
    	activated();
    }
    
    public void deactivate() {
    	active = false;
    	lastDeactivatedTime = god.time();
    	deactivated();
    }
    
    public boolean isActive() {
    	return active;
    }
    
    /**
	 * Called when the node is activated. Default does nothing.
	 * 
	 */
    protected void activated() {    	
    }
    
    /**
	 * Called when the node is deactivated. Default does nothing.
	 * 
	 */
    protected void deactivated() {    	
    }

    public abstract Data findData(K k);

    public abstract void storeData(Data<K> d, boolean nonvolatile);

    public abstract int nKeys();

    /**
	 * The bottom implementation feeds an agg. called "NodeNumbers" with...
	 */
    public void feedAggregator(DataAggregator da) {
        if (da.name().equals("NodeNumbers")) {
            da.next(num);
        }
    }
    
    /**
	 * Used by simulator to keep track of where the node is the in the array. Do
	 * not call.
	 * 
	 * @param n
	 *            The current place.
	 */
    final void setCurrentNumber(int n) {
    	currentNumber = n;
    }
    
    /**
	 * Used by simulator to keep track of where the node is the in the array. Do
	 * not call.
	 * 
	 * @return The current place.
	 */
    final int getCurrentNumber() {
    	return currentNumber;
    }
    
    
}
