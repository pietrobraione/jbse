package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceSymbolic;

/**
 * Class that wraps an instance of an object with class {@code java.lang.ClassLoader} 
 * or subclass in the heap, implementing copy-on-write.
 */
final class InstanceWrapper_JAVA_CLASSLOADER extends InstanceWrapper<InstanceImpl_JAVA_CLASSLOADER> implements Instance_JAVA_CLASSLOADER {
	/**
	 * Constructor.
	 * 
	 * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
	 *        must be put.
	 * @param destinationPosition the position in {@code destinationHeap} where
	 *        the clone must be put.
	 * @param delegate the initial delegate, the {@link InstanceImpl_JAVA_CLASSLOADER} that must be 
	 *        cloned upon writing.
	 */
	InstanceWrapper_JAVA_CLASSLOADER(Heap destinationHeap, long destinationPosition, InstanceImpl_JAVA_CLASSLOADER delegate) {
		super(destinationHeap, destinationPosition, delegate);
    }
	
	@Override
	public int classLoaderIdentifier() {
		return getDelegate().classLoaderIdentifier();
	}

	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.ClassLoader (or subclass).");
	}

	@Override
	public void makeInitial() throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeInitial an instance of java.lang.ClassLoader (or subclass).");
	}

	@Override
	public Instance_JAVA_CLASSLOADER clone() {
		//a wrapper shall never be cloned
		throw new UnexpectedInternalException("Tried to clone an InstanceWrapper_JAVA_CLASSLOADER.");
	}
}
