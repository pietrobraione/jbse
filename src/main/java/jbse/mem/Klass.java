package jbse.mem;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.KlassPseudoReference;

/**
 * Class that represents a Java class in the static
 * method area, i.e., its static fields.
 */
public final class Klass extends Objekt {
    /**
     * Constructor.
     * 
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution, 
     *        but rather assumed).
     * @param calc a {@link Calculator}.
     * @param origin a {@link KlassPseudoReference} if this {@link Klass}
     *        exists in the initial state, otherwise {@code null}.
     * @param epoch the creation {@link HistoryPoint} of this {@link Klass}.
     * @param fieldSignatures varargs of field {@link Signature}s.
     */
    Klass(boolean symbolic, Calculator calc, KlassPseudoReference origin, HistoryPoint epoch, Signature... fieldSignatures) {
    	super(symbolic, calc, "KLASS", origin, epoch, fieldSignatures);
    }
    
    @Override
    public Klass clone() {
    	final Klass o = (Klass) super.clone();
        o.fields = fieldsDeepCopy();
        
        return o;
    }
}
