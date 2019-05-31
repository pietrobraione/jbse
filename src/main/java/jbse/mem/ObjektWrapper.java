package jbse.mem;

import java.util.Collection;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * Class that wraps an {@link ObjektImpl}, implementing 
 * copy-on-write.
 * 
 * @author Pietro Braione
 *
 * @param <T> the type of the wrapped {@link ObjektImpl}.
 */
abstract class ObjektWrapper<T extends ObjektImpl> implements Objekt {
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
    protected ObjektWrapper(T delegate) {
    	this.delegate = delegate;
    	this.isDelegateAClone = false;
    }
    
    protected final void setDelegate(T delegate) {
    	this.delegate = delegate;
    }
    
    protected final T getDelegate() {
    	return this.delegate;
    }
    
    protected final boolean isDelegateAClone() {
    	return this.isDelegateAClone;
    }
    
    protected final void setDelegateIsAClone() {
    	this.isDelegateAClone = true;
    }
    
    protected abstract void possiblyCloneDelegate();
    
	@Override
	public final ClassFile getType() {
		return getDelegate().getType();
	}

	@Override
	public final ReferenceSymbolic getOrigin() {
		return getDelegate().getOrigin();
	}

	@Override
	public final HistoryPoint historyPoint() {
		return getDelegate().historyPoint();
	}

	@Override
	public final boolean isSymbolic() {
		return getDelegate().isSymbolic();
	}

	public abstract void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException;

	@Override
	public final void setIdentityHashCode(Primitive identityHashCode) {
		possiblyCloneDelegate();
		getDelegate().setIdentityHashCode(identityHashCode);
	}

	@Override
	public final Primitive getIdentityHashCode() {
		return getDelegate().getIdentityHashCode();
	}

	@Override
	public final Collection<Signature> getStoredFieldSignatures() {
		return getDelegate().getStoredFieldSignatures();
	}

	@Override
	public final boolean hasOffset(int slot) {
		return getDelegate().hasOffset(slot);
	}

	@Override
	public final Value getFieldValue(Signature sig) {
		return getDelegate().getFieldValue(sig);
	}

	@Override
	public final Value getFieldValue(String fieldName, String fieldClass) {
		return getDelegate().getFieldValue(fieldName, fieldClass);
	}

	@Override
	public final Value getFieldValue(int slot) {
		return getDelegate().getFieldValue(slot);
	}

	@Override
	public final void setFieldValue(Signature field, Value item) {
		possiblyCloneDelegate();
		getDelegate().setFieldValue(field, item);
	}

	@Override
	public final void setFieldValue(int slot, Value item) {
		possiblyCloneDelegate();
		getDelegate().setFieldValue(slot, item);
	}

	@Override
	public final Map<Signature, Variable> fields() {
		possiblyCloneDelegate(); //the returned Variables are mutable
		return getDelegate().fields();
	}
	
    @Override
    public abstract Objekt clone();
}