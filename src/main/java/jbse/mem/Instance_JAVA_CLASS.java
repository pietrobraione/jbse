package jbse.mem;

import static jbse.bc.Signatures.JAVA_CLASS;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent an instance of an object with class {@code java.lang.Class} 
 * in the heap.
 */
public final class Instance_JAVA_CLASS extends Instance {
    /** The java class it represents. Immutable. */
    private final ClassFile representedClass;
    
    protected Instance_JAVA_CLASS(Calculator calc, ClassFile cf_JAVA_CLASS, MemoryPath origin, Epoch epoch, ClassFile representedClass, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, cf_JAVA_CLASS, origin, epoch, numOfStaticFields, fieldSignatures);
        if (cf_JAVA_CLASS == null || !JAVA_CLASS.equals(cf_JAVA_CLASS.getClassName())) {
            throw new InvalidTypeException("Attempted creation of an instance of java.lang.Class with type " + classFile.getClassName());
        }
        this.representedClass = representedClass;
    }
    
    /**
     * Returns the class this {@code Instance}
     * of {@code java.lang.Class} represents.
     * 
     * @return a {@link ClassFile}, the  
     * represented class.
     */
    public ClassFile representedClass() {
        return this.representedClass;
    }
}
