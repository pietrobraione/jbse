package jbse.mem;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jbse.exc.common.UnexpectedInternalException;

/**
 * Class that offers the same services of the heap in the JVM's memory.
 */
final class Heap implements Cloneable {
    private SortedMap<Long, Objekt> objects; //TODO nonfinal to allow cloning
    private long nextIndex;
    
    /**
     * Constructor of an Heap structure.
     */
    Heap() {
        this.objects = new TreeMap<>();
        this.nextIndex = Util.POS_ROOT;
    }
    
    /**
     * Stores a new object into the heap.
     * 
     * @param item the {@link Objekt} to be stored in 
     *             the heap.
     * @return the position in the heap  
     *         where {@code item} is stored.
     */
    long addNew(Objekt item) {
        objects.put(this.nextIndex, item);
        long retVal = this.nextIndex;
        while (objects.containsKey(this.nextIndex)) {
        	++this.nextIndex;
        }
        return retVal;
    }
    
    /**
     * Sets an object into some heap location.
     * 
     * @param ref a {@code int}, the location where the object
     *        must be stored.
     * @param item the {@link Objekt} to stored at {@code pos}.
     * @throws UnexpectedInternalException 
     */
    void set(long pos, Objekt item) throws UnexpectedInternalException {
    	this.objects.put(pos, item);
    	//next free position, without garbage collection
        while (objects.containsKey(this.nextIndex)) {
        	if (this.nextIndex == Long.MAX_VALUE) {
        		throw new UnexpectedInternalException("Heap space exhausted.");
        	}
        	++this.nextIndex;
        }
    }
    
    /**
     * Gets an object from the heap.
     * 
     * @param pos a {@code long}, the location where the object
     *        must be stored.
     * @return the {@link Objekt} at position {@code pos}, or 
     *         {@code null} if nothing is stored at {@code pos}.
     **/
    Objekt getObject(long pos) {
        return this.objects.get(pos);
    }
    
    /**
     * Returns the objects in the heap as a {@link Map}.
     * 
     * @return an unmodifiable 
     * {@link Map}{@code <}{@link Long}{@code , }{@link Objekt}{@code >}
     * mapping heap positions to the {@link Objekt}s stored 
     * in them.
     */
    Map<Long, Objekt> getObjects() {
        return Collections.unmodifiableMap(this.objects);
    }    
    
    /**
     * Returns the number of objects in the heap.
     * 
     * @return a positive {@code int}.
     */
    int getSize() {
    	return this.objects.size();
    }
    
    @Override
    public String toString() {
        String tmpRet = "[";
        int j = 0;
        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
            tmpRet += e.getKey() + ":";
            tmpRet += e.getValue().toString();
            if (j < this.objects.size() - 1) 
            	tmpRet += ", ";
            j++;
        }
        tmpRet += "]";
        return(tmpRet);
    }
    
    @Override
    public Heap clone() {
        final Heap h;
        try {
            h = (Heap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        final SortedMap<Long, Objekt> objListClone = new TreeMap<>();
        
        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
        	Objekt val = e.getValue();
            objListClone.put(e.getKey(), val.clone());
        }
        h.objects = objListClone;
        return h;
    }
}