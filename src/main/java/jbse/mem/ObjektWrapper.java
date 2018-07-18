package jbse.mem;

/**
 * Class that wraps an {@link ObjektImpl}, implementing 
 * copy-on-write.
 * 
 * @author Pietro Braione
 *
 * @param <T> the type of the wrapped {@link ObjektImpl}.
 */
abstract class ObjektWrapper<T extends ObjektImpl> implements Objekt {
	private final Heap destinationHeap;
	private final long destinationPosition;
	private T delegate;
	private boolean isDelegateAClone;

	/**
	 * Constructor.
	 * 
	 * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
	 *        must be put.
	 * @param destinationPosition the position in {@code destinationHeap} where
	 *        the clone must be put.
	 * @param delegate the initial delegate, the {@link ObjektImpl} that must be 
	 *        cloned upon writing.
	 */
    protected ObjektWrapper(Heap destinationHeap, long destinationPosition, T delegate) {
    	this.delegate = delegate;
    	this.destinationHeap = destinationHeap;
    	this.destinationPosition = destinationPosition;
    	this.isDelegateAClone = false;
    }
    
    @SuppressWarnings("unchecked")
	protected final void possiblyCloneDelegate() {
    	//does nothing if the delegate is already a clone
    	if (this.isDelegateAClone) {
    		return;
    	}
    	//otherwise, clones the delegate and puts it in the heap
    	this.delegate = (T) this.delegate.clone();
    	this.destinationHeap.set(this.destinationPosition, this.delegate);
    	this.isDelegateAClone = true;
    }
    
    protected final long getDestinationPosition() {
    	return this.destinationPosition;
    }
    
    protected final T getDelegate() {
    	return this.delegate;
    }
    
    @Override
    public abstract Objekt clone();
}