package jbse.mem;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent an instance of an object with class {@code java.lang.Thread} 
 * in the heap.
 */
public final class Instance_JAVA_THREAD extends Instance {
    /** The interruption state of the thread. */
    private boolean interrupted;
    
    protected Instance_JAVA_THREAD(Calculator calc, ClassFile classFile, MemoryPath origin, Epoch epoch, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, classFile, origin, epoch, numOfStaticFields, fieldSignatures);
        if (classFile == null) {
            throw new InvalidTypeException("Attempted creation of an instance of a subclass of java.lang.Thread with type null.");
        }

        this.interrupted = false;
    }
    
    /**
     * Returns whether the thread this
     * object represents is interrupted.
     * 
     * @return a {@code boolean}.
     */
    public boolean isInterrupted() {
        return this.interrupted;
    }
    
    /**
     * Sets the interruption state of the
     * thread this object represents.
     * 
     * @param interrupted a {@code boolean}, 
     *        {@code true} iff the thread is 
     *        interrupted.
     */
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }
}
