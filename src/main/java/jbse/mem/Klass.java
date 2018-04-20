package jbse.mem;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.KlassPseudoReference;

/**
 * Class that represents a Java class in the static
 * method area, i.e., its static fields.
 */
public final class Klass extends Objekt {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param origin a {@link KlassPseudoReference} if this {@link Klass}
     *        exists in the initial state, otherwise {@code null}.
     * @param epoch the creation {@link Epoch} of this {@link Klass}.
     * @param fieldSignatures varargs of field {@link Signature}s.
     */
    Klass(Calculator calc, KlassPseudoReference origin, Epoch epoch, Signature... fieldSignatures) {
    	super(calc, "KLASS", origin, epoch, fieldSignatures);
    }
    
    @Override
    public Klass clone() {
    	final Klass o = (Klass) super.clone();
        o.fields = fieldsDeepCopy();
        
        return o;
    }
}
