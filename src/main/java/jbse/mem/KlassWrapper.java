package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceSymbolic;

/**
 * Abstract superclass of all the wrapper classes for objects that go in the heap.
 */
final class KlassWrapper extends ObjektWrapper<KlassImpl> implements Klass {
	private final StaticMethodArea destinationStaticArea;
	
	/**
	 * Constructor.
	 * 
	 * @param destinationStaticArea the {@link StaticMethodArea} where the clone of 
	 *        {@code delegate} must be put.
	 * @param delegate the initial delegate, the {@link KlassImpl} that must be 
	 *        cloned upon writing.
	 */
    KlassWrapper(StaticMethodArea destinationStaticArea, KlassImpl delegate) {
    	super(delegate);
    	this.destinationStaticArea = destinationStaticArea;
    }
    
	protected final void possiblyCloneDelegate() {
    	//does nothing if the delegate is already a clone
    	if (isDelegateAClone()) {
    		return;
    	}
    	//otherwise, clones the delegate and puts it in the static method area
    	setDelegate(getDelegate().clone());
    	setDelegateIsAClone();
    	this.destinationStaticArea.set(getDelegate());
    }

	@Override
	public boolean initializationStarted() {
		return getDelegate().initializationStarted();
	}

	@Override
	public boolean initializationCompleted() {
		return getDelegate().initializationCompleted();
	}

	@Override
	public void setInitializationStarted() {
		possiblyCloneDelegate();
		getDelegate().setInitializationStarted();
	}

	@Override
	public void setInitializationCompleted() {
		possiblyCloneDelegate();
		getDelegate().setInitializationCompleted();
	}
    
	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		possiblyCloneDelegate();
		getDelegate().makeSymbolic(origin);
	}

    @Override
    public Klass clone() {
        //a wrapper shall never be cloned
        throw new UnexpectedInternalException("Tried to clone a KlassWrapper.");
    }
}