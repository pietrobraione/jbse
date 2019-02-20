package jbse.mem;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * Class that implements the heap in the JVM's memory.
 */
final class Heap implements Cloneable {
    private final long maxHeapSize;
	private Heap delegate; //TODO nonfinal to allow cloning
    private SortedMap<Long, Objekt> objects; //TODO nonfinal to allow cloning
    private long nextIndex;
    
    private static final class PleaseLookInDelegateHeap implements Objekt {
    	private static final PleaseLookInDelegateHeap INSTANCE = new PleaseLookInDelegateHeap();
		static PleaseLookInDelegateHeap instance() { return INSTANCE; }
		
		private PleaseLookInDelegateHeap() { }
		
		private static final String ERROR_USE = "Attempted to use class Heap.PleaseLookInDelegateHeap as it were a true Objekt.";

		@Override
		public ClassFile getType() { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public ReferenceSymbolic getOrigin() { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public HistoryPoint historyPoint() { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public boolean isSymbolic() { throw new UnexpectedInternalException(ERROR_USE); }
		
		@Override
		public void makeSymbolic(ReferenceSymbolic origin) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public void setIdentityHashCode(Primitive identityHashCode) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Primitive getIdentityHashCode() { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Collection<Signature> getStoredFieldSignatures() { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public boolean hasSlot(int slot) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Value getFieldValue(Signature sig) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Value getFieldValue(String fieldName, String fieldClass) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Value getFieldValue(int slot) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public int getFieldSlot(Signature field) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public void setFieldValue(Signature field, Value item) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public void setFieldValue(int slot, Value item) { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Map<Signature, Variable> fields() { throw new UnexpectedInternalException(ERROR_USE); }

		@Override
		public Objekt clone() { throw new UnexpectedInternalException(ERROR_USE); }
    }

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
     * @param item the {@link ObjektImpl} to be stored in 
     *        the heap.
     * @return the position in the heap  
     *         where {@code item} is stored.
     * @throws HeapMemoryExhaustedException if the heap
     *         cannot store any more object.
     */
    long addNew(ObjektImpl item) throws HeapMemoryExhaustedException {
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
    long addNewSurely(ObjektImpl item) {
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
        final Objekt localObjekt = this.objects.get(pos);
        if (localObjekt == PleaseLookInDelegateHeap.instance()) {
        	final ObjektImpl trueObjekt = getTheRealThing(pos);
        	final ObjektWrapper<?> delegateObjekt = trueObjekt.makeWrapper(this, pos);
        	set(pos, delegateObjekt);
        	return delegateObjekt;
        } else {
        	return localObjekt;
        }
    }
    
    private ObjektImpl getTheRealThing(long pos) {
        final Objekt localObjekt = this.objects.get(pos);
        if (localObjekt == PleaseLookInDelegateHeap.instance()) {
        	return this.delegate.getTheRealThing(pos);
        } else if (localObjekt instanceof ObjektWrapper<?>) {
        	return ((ObjektWrapper<?>) localObjekt).getDelegate();
        } else {
        	return (ObjektImpl) localObjekt;
        }
    }
    
    private void makeAllWrappers() {
        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
            if (e.getValue() == PleaseLookInDelegateHeap.instance()) {
            	final long pos = e.getKey();
            	final ObjektImpl trueObjekt = getTheRealThing(pos);
            	final ObjektWrapper<?> delegateObjekt = trueObjekt.makeWrapper(this, pos);
            	e.setValue(delegateObjekt);
            }
        }
    }

	public long getNextFreePosition() {
		return this.nextIndex;
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
        return this.objects.size();
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
        for (Iterator<Map.Entry<Long, Objekt>> it = this.objects.entrySet().iterator(); it.hasNext(); ) {
            if (exceptPos.contains(it.next().getKey())) {
                continue;
            }
            it.remove();
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
        final SortedMap<Long, Objekt> objectsClone = new TreeMap<>();
        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
            objectsClone.put(e.getKey(), PleaseLookInDelegateHeap.instance());
        }
        h.objects = objectsClone;
        
        return h;
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

        h.delegate = null;
        final SortedMap<Long, Objekt> objectsClone = new TreeMap<>();
        for (Map.Entry<Long, Objekt> e : this.objects.entrySet()) {
            objectsClone.put(e.getKey(), getTheRealThing(e.getKey()).clone());
        }
        h.objects = objectsClone;
        
        return h;
    }
}