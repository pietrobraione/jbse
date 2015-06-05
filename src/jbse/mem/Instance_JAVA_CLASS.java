package jbse.mem;

import static jbse.bc.Signatures.JAVA_CLASS;

import jbse.bc.Signature;
import jbse.val.Calculator;

/**
 * Class that represent an instance of an object with class {@code java.lang.Class} 
 * in the heap.
 */
public final class Instance_JAVA_CLASS extends Instance {
    /** The java class it represents. Immutable. */
    private final String representedClass;

    protected Instance_JAVA_CLASS(Calculator calc, String origin, Epoch epoch, String representedClass, Signature... fieldSignatures) {
        super(calc, JAVA_CLASS, origin, epoch, fieldSignatures);
        this.representedClass = representedClass;
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
}
