package jbse.mem;

import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASS_CLASSLOADER;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.Null;

/**
 * Class that represent an instance of an object with class {@code java.lang.Class} 
 * in the heap.
 */
public final class Instance_JAVA_CLASS extends Instance {
    /** The java class it represents. Immutable. */
    private final String representedClass;
    
    /** 
     * Whether the class is for a primitive type including 
     * {@code void} or not. Immutable. 
     */
    private final boolean isPrimitive;

    protected Instance_JAVA_CLASS(Calculator calc, MemoryPath origin, Epoch epoch, String representedClass, boolean isPrimitive, int numOfStaticFields, Signature... fieldSignatures) {
        super(calc, JAVA_CLASS, origin, epoch, numOfStaticFields, fieldSignatures);
        this.representedClass = representedClass;
        this.isPrimitive = isPrimitive;
        setFieldValue(JAVA_CLASS_CLASSLOADER, Null.getInstance()); //possibly pleonastic
    }
    
    /**
     * Returns the name of the class this {@code Instance}
     * of {@code java.lang.Class} represents.
     * 
     * @return a {@code String}, the name of the 
     * represented class.
     */
    public String representedClass() {
        return this.representedClass;
    }
    
    /**
     * Checks whether this {@code Instance}
     * of {@code java.lang.Class} represent a
     * primitive type.
     * 
     * @return iff this object represents
     * a primitive type or {@code void}.
     */
    public boolean isPrimitive() {
        return this.isPrimitive;
    }
}
