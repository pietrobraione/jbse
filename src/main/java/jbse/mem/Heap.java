package jbse.mem;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.HeapMemoryExhaustedException;

/**
 * Class that implements the heap in the JVM's memory.
 */
final class Heap implements Cloneable {
    private final long maxHeapSize;
	private Heap delegate; //nonfinal to allow cloning
    private TreeMap<Long, HeapObjekt> objects; //nonfinal to allow cloning
    private long nextIndex;
    
    /**
     * Constructor.
     * 
     * @param maxHeapSize an {@code int}, the maximum number
     *        of objects this heap can store.
     */
    Heap(long maxHeapSize) {
    	this.delegate = null;
        this.maxHeapSize = maxHeapSize;
        this.objects = new TreeMap<>();
        this.nextIndex = Util.POS_ROOT;
    }

    /**
     * Stores a new object into the heap up to the
     * maximum capacity of the heap.
     * 
     * @param item the {@link InstanceImpl} to be stored in 
     *        the heap.
     * @return the position in the heap  
     *         where {@code item} is stored.
     * @throws HeapMemoryExhaustedException if the heap
     *         cannot store any more object.
     */
    long addNew(HeapObjektImpl item) throws HeapMemoryExhaustedException {
        if (this.objects.size() >= this.maxHeapSize) {
            throw new HeapMemoryExhaustedException();
        }
        return addNewSurely(item);
    }

    /**
     * Stores a new object into the heap. This operation
     * always succeeds.
     * 
     * @param item the {@link InstanceImpl} to be stored in 
     *        the heap.
     * @return the position in the heap  
     *         where {@code item} is stored.
     */
    long addNewSurely(HeapObjektImpl item) {
        this.objects.put(this.nextIndex, item);
        long retVal = this.nextIndex;
        while (existsAt(this.nextIndex)) {
            if (this.nextIndex == Long.MAX_VALUE) {
                throw new UnexpectedInternalException("Heap space exhausted.");
            }
            ++this.nextIndex;
        }
        return retVal;
    }
    
    /**
     * Checks whether there is an object at some position.
     * 
     * @param index a {@code long}.
     * @return {@code true} iff there is an object at position {@code index}.
     */
    boolean existsAt(long index) {
    	if (this.objects.containsKey(index)) {
    		return (this.objects.get(index) != null);
    	} else {
    		return (this.delegate != null && this.delegate.existsAt(index));
    	}
    }

    /**
     * Sets an object into some heap location.
     * 
     * @param ref a {@code long}, the location where the instance
     *        must be stored.
     * @param item the {@link HeapObjekt} to stored at {@code pos}.
     */
    void set(long pos, HeapObjekt item) {
        this.objects.put(pos, item);
    }

    /**
     * Gets an object from the heap.
     * 
     * @param pos a {@code long}, the location where the object
     *        must be stored.
     * @return the {@link HeapObjekt} at position {@code pos}, or 
     *         {@code null} if nothing is stored at {@code pos}.
     */
    HeapObjekt getObject(long pos) {
    	if (existsAt(pos)) {
    		final HeapObjekt localObjekt = this.objects.get(pos);
    		if (localObjekt == null) {
    			final HeapObjektImpl trueObjekt = getTheRealThing(pos);
    			final HeapObjektWrapper<?> delegateObjekt = trueObjekt.makeWrapper(this, pos);
    			set(pos, delegateObjekt);
    			return delegateObjekt;
    		} else {
    			return localObjekt;
    		}
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the real {@link ObjektImpl} that is stored
     * at some position.
     * 
     * @param pos a {@code long}. It must be 
     * {@link #existsAt(long) existsObjectAt}{@code (pos) == true}.
     * @return the {@link ObjektImpl} stored at {@code pos}, obtained
     * by walking through the delegation chain until either an {@link ObjektImpl}
     * or an {@link ObjektWrapper} is found (in the latter case, the wrapped
     * object is returned).
     */
    private HeapObjektImpl getTheRealThing(long pos) {
		final HeapObjekt localObjekt = this.objects.get(pos);
		if (localObjekt == null) {
			return this.delegate.getTheRealThing(pos);
		} else if (localObjekt instanceof ObjektWrapper<?>) {
			return ((HeapObjektWrapper<?>) localObjekt).getDelegate();
		} else {
			return (HeapObjektImpl) localObjekt;
		}
    }
    
    private TreeSet<Long> filledPositions() {
    	final TreeSet<Long> retVal = new TreeSet<>();
    	for (long pos : this.objects.keySet()) {
    		if (this.objects.get(pos) != null) {
    			retVal.add(pos);
    		}
    	}
    	if (this.delegate != null) {
    		retVal.addAll(this.delegate.filledPositions());
    	}
    	return retVal;
    }
    
    private void makeAllWrappers() {
        for (long pos : filledPositions()) {
            if (!this.objects.containsKey(pos)) {
            	final HeapObjektImpl trueObjekt = getTheRealThing(pos);
            	final HeapObjektWrapper<?> delegateObjekt = trueObjekt.makeWrapper(this, pos);
            	this.objects.put(pos, delegateObjekt);
            }
        }
    }

    /**
     * Returns the objects in the heap as a {@link Map}.
     * 
     * @return a 
     * {@link SortedMap}{@code <}{@link Long}{@code , }{@link Objekt}{@code >}
     * mapping heap positions to the {@link Objekt}s stored 
     * at them.
     */
    SortedMap<Long, Objekt> getObjects() {
    	makeAllWrappers();
        return new TreeMap<>(this.objects);
    }    

    /**
     * Returns the number of objects in the heap.
     * 
     * @return a positive {@code int}.
     */
    int getSize() {
        return filledPositions().size();
    }
    
    /**
     * Deletes objects from this heap.
     * 
     * @param exceptPos a {@link Set}{@code <}{@link Long}{@code >}.
     *        The objects at positions in {@code except}
     *        will not be deleted, all the remaining objects
     *        will.
     */
    void disposeExcept(Set<Long> exceptPos) {
        for (long pos : filledPositions()) {
            if (exceptPos.contains(pos)) {
                continue;
            }
            this.objects.put(pos, null);
        }
    }

    Heap lazyClone() {
        final Heap h;
        try {
            h = (Heap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        h.delegate = this;
        h.objects = new TreeMap<>();
        
        return h;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[");
        boolean isFirst = true;
        for (long pos : filledPositions()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }
            buf.append(pos);
            buf.append(":");
            buf.append(getObject(pos).toString());
        }
        buf.append("]");
        return buf.toString();
    }

    @Override
    public Heap clone() {
        final Heap h;
        try {
            h = (Heap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        h.delegate = null;
        final TreeMap<Long, HeapObjekt> objectsClone = new TreeMap<>();
        for (long pos : filledPositions()) {
            objectsClone.put(pos, getTheRealThing(pos).clone());
        }
        h.objects = objectsClone;
        
        return h;
    }
}