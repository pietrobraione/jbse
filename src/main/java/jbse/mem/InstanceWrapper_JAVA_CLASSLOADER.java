package jbse.mem;


import java.util.Collection;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * Class that wraps an instance of an object with class {@code java.lang.ClassLoader} 
 * or subclass in the heap, implementing copy-on-write.
 */
final class InstanceWrapper_JAVA_CLASSLOADER extends ObjektWrapper<InstanceImpl_JAVA_CLASSLOADER> implements Instance_JAVA_CLASSLOADER {
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
	public ClassFile getType() {
		return getDelegate().getType();
	}

	@Override
	public ReferenceSymbolic getOrigin() {
		return getDelegate().getOrigin();
	}

	@Override
	public HistoryPoint historyPoint() {
		return getDelegate().historyPoint();
	}

	@Override
	public boolean isSymbolic() {
		return getDelegate().isSymbolic();
	}

	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.ClassLoader (or subclass).");
	}

	@Override
	public void setIdentityHashCode(Primitive identityHashCode) {
		possiblyCloneDelegate();
		getDelegate().setIdentityHashCode(identityHashCode);
	}

	@Override
	public Primitive getIdentityHashCode() {
		return getDelegate().getIdentityHashCode();
	}

	@Override
	public Collection<Signature> getStoredFieldSignatures() {
		return getDelegate().getStoredFieldSignatures();
	}

	@Override
	public boolean hasSlot(int slot) {
		return getDelegate().hasSlot(slot);
	}

	@Override
	public Value getFieldValue(Signature sig) {
		return getDelegate().getFieldValue(sig);
	}

	@Override
	public Value getFieldValue(String fieldName, String fieldClass) {
		return getDelegate().getFieldValue(fieldName, fieldClass);
	}

	@Override
	public Value getFieldValue(int slot) {
		return getDelegate().getFieldValue(slot);
	}

	@Override
	public int getFieldSlot(Signature field) {
		return getDelegate().getFieldSlot(field);
	}

	@Override
	public void setFieldValue(Signature field, Value item) {
		possiblyCloneDelegate();
		getDelegate().setFieldValue(field, item);
	}

	@Override
	public void setFieldValue(int slot, Value item) {
		possiblyCloneDelegate();
		getDelegate().setFieldValue(slot, item);
	}

	@Override
	public Map<Signature, Variable> fields() {
		return getDelegate().fields();
	}

	@Override
	public Instance_JAVA_CLASSLOADER clone() {
		//a wrapper shall never be cloned
		throw new UnexpectedInternalException("Tried to clone an InstanceWrapper_JAVA_CLASSLOADER.");
	}
}
