package jbse.mem;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that implements {@link Instance_JAVA_THREAD}. 
 */
public final class InstanceImpl_JAVA_THREAD extends InstanceImpl implements Instance_JAVA_THREAD {
    /** The interruption state of the thread. */
    private boolean interrupted;
    
    protected InstanceImpl_JAVA_THREAD(Calculator calc, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, false, classFile, origin, epoch, numOfStaticFields, fieldSignatures);
        if (classFile == null) {
            throw new InvalidTypeException("Attempted creation of an instance of a subclass of java.lang.Thread with type null.");
        }

        this.interrupted = false;
    }
    
    @Override
    InstanceWrapper_JAVA_THREAD makeWrapper(Heap destinationHeap, long destinationPosition) {
    	return new InstanceWrapper_JAVA_THREAD(destinationHeap, destinationPosition, this);
    }
    
    @Override
    public boolean isInterrupted() {
        return this.interrupted;
    }
    
    @Override
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }
    
	@Override
	public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.Thread (or subclass).");
	}

    @Override
    public InstanceImpl_JAVA_THREAD clone() {
    	return (InstanceImpl_JAVA_THREAD) super.clone();
    }
}
