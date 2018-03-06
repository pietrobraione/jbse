package jbse.mem;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.HeapMemoryExhaustedException;

/**
 * Class that implements the heap in the JVM's memory.
 */
final class Heap implements Cloneable {
    private final long maxHeapSize;
    private SortedMap<Long, Objekt> objects; //TODO nonfinal to allow cloning
    private long nextIndex;

    /**
     * Constructor.
     * 
     * @param maxHeapSize an {@code int}, the maximum number
     *        of objects this heap can store.
     */
    Heap(long maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
        this.objects = new TreeMap<>();
        this.nextIndex = Util.POS_ROOT;
    }

    /**
     * Stores a new object into the heap up to the
     * maximum capacity of the heap.
     * 
     * @param item the {@link Objekt} to be stored in 
     *        the heap.
     * @return the position in the heap  
     *         where {@code item} is stored.
     * @throws HeapMemoryExhaustedException if the heap
     *         cannot store any more object.
     */
    long addNew(Objekt item) throws HeapMemoryExhaustedException {
        if (this.objects.size() >= this.maxHeapSize) {
            throw new HeapMemoryExhaustedException();
        }
        return addNewSurely(item);
    }

    /**
     * Stores a new object into the heap. This operation
     * always succeeds.
     * 
     * @param item the {@link Objekt} to be stored in 
     *        the heap.
     * @return the position in the heap  
     *         where {@code item} is stored.
     */
    long addNewSurely(Objekt item) {
        this.objects.put(this.nextIndex, item);
        long retVal = this.nextIndex;
        while (this.objects.containsKey(this.nextIndex)) {
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
     */
    void set(long pos, Objekt item) {
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
     * @return a 
     * {@link SortedMap}{@code <}{@link Long}{@code , }{@link Objekt}{@code >}
     * mapping heap positions to the {@link Objekt}s stored 
     * at them.
     */
    SortedMap<Long, Objekt> getObjects() {
        return new TreeMap<>(this.objects);
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
        final StringBuilder buf = new StringBuilder();
        buf.append("[");
        boolean isFirst = true;
        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }
            buf.append(e.getKey());
            buf.append(":");
            buf.append(e.getValue().toString());
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

        final SortedMap<Long, Objekt> objListClone = new TreeMap<>();

        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
            final Objekt val = e.getValue();
            objListClone.put(e.getKey(), val.clone());
        }
        h.objects = objListClone;
        return h;
    }
}