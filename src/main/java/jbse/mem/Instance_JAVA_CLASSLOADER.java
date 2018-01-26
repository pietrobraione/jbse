package jbse.mem;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent an instance of an object with class {@code java.lang.ClassLoader} 
 * in the heap.
 */
public final class Instance_JAVA_CLASSLOADER extends Instance {
    /** The identifier of this classloader. It must be >= 1. */
    private final int classLoaderIdentifier;
    
    protected Instance_JAVA_CLASSLOADER(Calculator calc, ClassFile classFile, MemoryPath origin, Epoch epoch, int classLoaderIdentifier, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, classFile, origin, epoch, numOfStaticFields, fieldSignatures);
        if (classFile == null) {
            throw new InvalidTypeException("Attempted creation of an instance of java.lang.Class with type null.");
        }

        this.classLoaderIdentifier = classLoaderIdentifier;
    }
    
    /**
     * Returns the identifier of this
     * {@code java.lang.ClassLoader}.
     * 
     * @return an {@code int}, the identifier of 
     * this classloader.
     */
    public int classLoaderIdentifier() {
        return this.classLoaderIdentifier;
    }
}
