package jbse.mem;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.MemoryPath;

/**
 * Class that represents a Java class in the static
 * method area, i.e., its static fields.
 */
public final class Klass extends Objekt {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param origin a {@link MemoryPath}, the
     *        chain of memory accesses which allowed to discover
     *        the object for the first time. It can be null when
     *        {@code epoch == }{@link Epoch#EPOCH_AFTER_START}.
     * @param epoch the creation {@link Epoch} of this {@link Klass}.
     * @param fieldSignatures varargs of field {@link Signature}s.
     */
    Klass(Calculator calc, MemoryPath origin, Epoch epoch, Signature... fieldSignatures) {
    	super(calc, "KLASS", origin, epoch, fieldSignatures);
    }
    
    @Override
    public Klass clone() {
    	final Klass o = (Klass) super.clone();
        o.fields = fieldsDeepCopy();
        
        return o;
    }
}
