package simsalabim;

public class Data<K extends Key> implements InfoObject {

    private long size;
    private K key;

    private long createTime;
    private long placeTime = -1;
    private long deathTime = -1;
    private long forgetTime = -1;

    private int storeCount = 0;
    private int retrieveCount = 0;

    // For network partitioning
    private int sourceNetwork = 0;

    public Data(Simulator god, K key, long size) {
    	this(key, size, god.time());
    }

    public Data(K key, long size, long createTime) {
        this.key = key;
        this.size = size;
        this.createTime = createTime;
    }

    public K key() {
        return key;
    }

    public long size() {
        return size;
    }

    public void addedTo(Node n) {
    	if (deathTime != -1)
    		System.err.println("Added to node although deathtime set " + this);
        if (placeTime == -1) {
            if (storeCount != 0)
                throw new SimulationException("Storage Count of data object " +
                                              "not zero on insert!");
            placeTime = n.time();
        }
        storeCount++;
    }

    public void removedFrom(Node n) {
        if (storeCount == 0)
            throw new SimulationException("Data removed more times than stored"
                                          + ". Node: " + n);
        storeCount--;
        
        if (storeCount == 0)
            deathTime = n.time();
    }
    
    public void forgotten(long time) {
    	this.forgetTime = time;
    }
    
    public void retrieved() {
    	this.retrieveCount++;
    }

    public int fieldnum() {
        return 8;
    }

    public void sourceNetwork(int id) {
	    sourceNetwork = id;
    }

    public int sourceNetwork() {
	    return sourceNetwork;
    }

    // Info fields
    private static final String[] flds = {
        "Key",
        "Size",
        "Created",
        "Placed",
        "Died",
        "Forgotten",
        "Node Count",
        "RetrieveCount"
    };

    public String[] fields() {
        return flds;
    }

    public String[] info() {
        return new String[] {
            key.stringVal(),
            Long.toString(size),
            Long.toString(createTime),
            Long.toString(placeTime),
            Long.toString(deathTime),
            Long.toString(forgetTime),
            Long.toString(storeCount),
            Long.toString(retrieveCount)
        };
    }
    
    
    public String toString() {
        return "D/" + "K:" + key.toString() + "/S:" + size + "/";
    }

}
