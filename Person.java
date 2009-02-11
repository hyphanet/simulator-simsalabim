package simsalabim;
import java.util.*;

    /**
	 * Class for a node in social network.
	 */
class Person implements Comparable<Person> {
    
    public LinkedList<Person> outN = new LinkedList<Person>();
    public int id;
    private boolean does_darknet = false;
    private boolean does_opennet = false;

    public int netid = 0;  // Network partition
    public int score = 0;
    private DarknetNode node;
    
    public Person(int id) {
	this.id = id;
    }

    public Iterator<Person> neighbors() {
	return outN.iterator();
    }

    public boolean isOpennet() {
	if (!does_darknet && !does_opennet) throw new SimulationException ("Node " + this + " without type.");	    
	return does_opennet;
    }

    public boolean isDarknet() {
	if (!does_darknet && !does_opennet) throw new SimulationException ("Node " + this + " without type.");	    
	return does_darknet;
    }

    public int degree() {
	    return outN.size();
    }

    public int network() {
	    return netid;
    }

    public void setNetwork(int id) {
	    netid = id;
    }

    public void setDarknet() {
        does_darknet = true;
    }

    public void setOpennet() {
	does_opennet = true;
    }

    public void joinedNetwork(DarknetNode node) {
	this.node = node;
    }

    public void leftNetwork() {
	this.node = null;
    }

    public boolean isInNetwork() {
	return node != null;
    }

    public DarknetNode getNode() {
	return node;
    }

    public void addOut(Person n) {
	if (!outN.contains(n))
	    outN.add(n);
    }
    
    public boolean linksTo(Person n) {
	return outN.contains(n);
    }
    
    public void removeNeighbor(Person n) {
	outN.remove(n);
    }

    
    public int compareTo(Person o) {
	return ( score > o.score ? 
		 1 : 
		 ( score < o.score ?
		   -1 :
		   ( id > o.id ?
		     1:
		     ( id < o.id ?
			   -1:
		       0))));
    }

    public boolean equals(Object o) {
	return ((Person) o).id == id && ((Person) o).score == score;
    }

}
