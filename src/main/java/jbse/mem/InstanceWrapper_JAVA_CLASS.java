package jbse.mem;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

/**
 * Class that wraps an instance of an object with class {@code java.lang.Class} 
 * in the heap, implementing copy-on-write.
 */
final class InstanceWrapper_JAVA_CLASS extends InstanceWrapper<InstanceImpl_JAVA_CLASS> implements Instance_JAVA_CLASS {
	/**
	 * Constructor.
	 * 
	 * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
	 *        must be put.
	 * @param destinationPosition the position in {@code destinationHeap} where
	 *        the clone must be put.
	 * @param delegate the initial delegate, the {@link InstanceImpl_JAVA_CLASS} that must be 
	 *        cloned upon writing.
	 */
	InstanceWrapper_JAVA_CLASS(Heap destinationHeap, long destinationPosition, InstanceImpl_JAVA_CLASS delegate) {
		super(destinationHeap, destinationPosition, delegate);
    }
	
	@Override
	public ClassFile representedClass() {
		return getDelegate().representedClass();
	}
	
	@Override
	public void setSigners(ReferenceConcrete signers) throws InvalidInputException {
		possiblyCloneDelegate();
	    getDelegate().setSigners(signers); 
	}
	
	@Override
	public ReferenceConcrete getSigners() {
	    return getDelegate().getSigners();
	}

	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.Class.");
	}
	
	@Override
	public void makeInitial() throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeInitial an instance of java.lang.Class.");
	}

	@Override
	public Instance_JAVA_CLASS clone() {
		//a wrapper shall never be cloned
		throw new UnexpectedInternalException("Tried to clone an InstanceWrapper_JAVA_CLASS.");
	}
}
