package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceSymbolic;

/**
 * Class that wraps an instance of a meta level box 
 * in the heap, implementing copy-on-write.
 */
final class InstanceWrapper_METALEVELBOX extends InstanceWrapper<InstanceImpl_METALEVELBOX> implements Instance_METALEVELBOX {
    /**
     * Constructor.
     * 
     * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
     *        must be put.
     * @param destinationPosition the position in {@code destinationHeap} where
     *        the clone must be put.
     * @param delegate the initial delegate, the {@link InstanceImpl_METALEVELBOX} that must be 
     *        cloned upon writing.
     */
	InstanceWrapper_METALEVELBOX(Heap destinationHeap, long destinationPosition, InstanceImpl_METALEVELBOX delegate) {
        super(destinationHeap, destinationPosition, delegate);
    }

	@Override
	public Object get() {
		return getDelegate().get();
	}

	@Override
	public void makeInitial() throws InvalidInputException {
        throw new InvalidInputException("Attempted to makeInitial a meta-level box.");
	}

	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
        throw new InvalidInputException("Attempted to makeSymbolic a meta-level box.");
		
	}

	@Override
	public InstanceWrapper_METALEVELBOX clone() {
		//a wrapper shall never be cloned
		throw new UnexpectedInternalException("Attempted to clone an InstanceWrapper_METALEVELBOX.");
	}
}
