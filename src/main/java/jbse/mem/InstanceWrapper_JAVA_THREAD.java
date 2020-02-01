package jbse.mem;


import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceSymbolic;

/**
 * Class that wraps an instance of an object with class {@code java.lang.Thread} 
 * or subclass in the heap, implementing copy-on-write.
 */
final class InstanceWrapper_JAVA_THREAD extends InstanceWrapper<InstanceImpl_JAVA_THREAD> implements Instance_JAVA_THREAD {
    /**
     * Constructor.
     * 
     * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
     *        must be put.
     * @param destinationPosition the position in {@code destinationHeap} where
     *        the clone must be put.
     * @param delegate the initial delegate, the {@link InstanceImpl_JAVA_THREAD} that must be 
     *        cloned upon writing.
     */
    InstanceWrapper_JAVA_THREAD(Heap destinationHeap, long destinationPosition, InstanceImpl_JAVA_THREAD delegate) {
        super(destinationHeap, destinationPosition, delegate);
    }

    @Override
    public boolean isInterrupted() {
        return getDelegate().isInterrupted();
    }

    @Override
    public void setInterrupted(boolean interrupted) {
        possiblyCloneDelegate();
        getDelegate().setInterrupted(interrupted);
    }

    @Override
    public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
        throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.Thread (or subclass).");
    }

	@Override
	public void makeInitial() throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeInitial an instance of java.lang.Thread (or subclass).");
	}

	@Override
	public final Instance_JAVA_THREAD clone() {
		//a wrapper shall never be cloned
		throw new UnexpectedInternalException("Attempted to clone an InstanceWrapper_JAVA_THREAD.");
	}
}
