package jbse.mem;

/**
 * Abstract superclass of all the wrapper classes for instances.
 */
abstract class InstanceWrapper<T extends InstanceImpl> extends HeapObjektWrapper<T> implements Instance {
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
    InstanceWrapper(Heap destinationHeap, long destinationPosition, T delegate) {
    	super(destinationHeap, destinationPosition, delegate);
    }    
	
    @Override
    public abstract Instance clone();
}