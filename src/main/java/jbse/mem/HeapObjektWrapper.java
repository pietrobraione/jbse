package jbse.mem;

import jbse.common.exc.InvalidInputException;

/**
 * Abstract superclass of all the wrapper classes for objects that go in the heap.
 */
abstract class HeapObjektWrapper<T extends HeapObjektImpl> extends ObjektWrapper<T> implements HeapObjekt {
	private final Heap destinationHeap;
	private final long destinationPosition;
	
	/**
	 * Constructor.
	 * 
	 * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
	 *        must be put.
	 * @param destinationPosition the position in {@code destinationHeap} where
	 *        the clone must be put.
	 * @param delegate the initial delegate, the {@link InstanceImpl} that must be 
	 *        cloned upon writing.
	 */
    HeapObjektWrapper(Heap destinationHeap, long destinationPosition, T delegate) {
    	super(delegate);
    	this.destinationHeap = destinationHeap;
    	this.destinationPosition = destinationPosition;
    }
    
    @SuppressWarnings("unchecked")
	protected final void possiblyCloneDelegate() {
    	//does nothing if the delegate is already a clone
    	if (isDelegateAClone()) {
    		return;
    	}
    	//otherwise, clones the delegate and puts it in the heap
    	setDelegate((T) getDelegate().clone());
    	setDelegateIsAClone();
    	this.destinationHeap.set(this.destinationPosition, getDelegate());
    }
    
    protected final long getDestinationPosition() {
    	return this.destinationPosition;
    }
    
    @Override
    public final boolean isInitial() {
    	return getDelegate().isInitial();
    }
	
	public abstract void makeInitial() throws InvalidInputException;

    @Override
    public abstract HeapObjekt clone();
}