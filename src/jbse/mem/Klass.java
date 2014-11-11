package jbse.mem;

import jbse.bc.Signature;

/**
 * Class that represents a Java class in memory, i.e., its 
 * static fields.
 */
public class Klass extends Instance {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param fieldSignatures an array of field {@link Signature}s.
     * @param origin the origin of this {@code Klass}, or {@code null}
     *        iff the {@code Klass} has not been created by lazy
     *        initialization.
     */
    Klass(Calculator calc, Signature[] fieldSignatures, String origin, Epoch epoch) {
    	super(calc, fieldSignatures, "KLASS", origin, epoch);
    }
    
    @Override
    public Klass clone() {
    	return (Klass) super.clone();
    }
}
