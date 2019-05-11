package jbse.mem;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that implements {@link Instance_JAVA_CLASSLOADER}. 
 */
public final class InstanceImpl_JAVA_CLASSLOADER extends InstanceImpl implements Instance_JAVA_CLASSLOADER {
    /** The identifier of this classloader. It must be >= 1. */
    private final int classLoaderIdentifier;
    
    protected InstanceImpl_JAVA_CLASSLOADER(Calculator calc, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, int classLoaderIdentifier, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, false, classFile, origin, epoch, numOfStaticFields, fieldSignatures);
        if (classFile == null) {
            throw new InvalidTypeException("Attempted creation of an instance of a subclass of java.lang.ClassLoader with type null.");
        }

        this.classLoaderIdentifier = classLoaderIdentifier;
    }
    
    @Override
    InstanceWrapper_JAVA_CLASSLOADER makeWrapper(Heap destinationHeap, long destinationPosition) {
    	return new InstanceWrapper_JAVA_CLASSLOADER(destinationHeap, destinationPosition, this);
    }

    @Override
    public int classLoaderIdentifier() {
        return this.classLoaderIdentifier;
    }
    
	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.ClassLoader (or subclass).");
	}

    @Override
    public InstanceImpl_JAVA_CLASSLOADER clone() {
    	return (InstanceImpl_JAVA_CLASSLOADER) super.clone();
    }
}
