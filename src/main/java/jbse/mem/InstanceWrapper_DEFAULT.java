package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceSymbolic;

/**
 * Class that wraps an instance of an object in the heap, implementing 
 * copy-on-write.
 */
final class InstanceWrapper_DEFAULT extends InstanceWrapper<InstanceImpl_DEFAULT> implements Instance {
	/**
	 * Constructor.
	 * 
	 * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
	 *        must be put.
	 * @param destinationPosition the position in {@code destinationHeap} where
	 *        the clone must be put.
	 * @param delegate the initial delegate, the {@link InstanceImpl_DEFAULT} that must be 
	 *        cloned upon writing.
	 */
    InstanceWrapper_DEFAULT(Heap destinationHeap, long destinationPosition, InstanceImpl_DEFAULT delegate) {
    	super(destinationHeap, destinationPosition, delegate);
    }
    
	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		possiblyCloneDelegate();
		getDelegate().makeSymbolic(origin);
	}

	@Override
	public void makeInitial() throws InvalidInputException {
		possiblyCloneDelegate();
		getDelegate().makeInitial();
	}

	@Override
	public final Instance clone() {
		//a wrapper shall never be cloned
		throw new UnexpectedInternalException("Attempted to clone an InstanceWrapper.");
	}
}