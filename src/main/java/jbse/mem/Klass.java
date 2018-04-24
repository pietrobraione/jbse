package jbse.mem;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.KlassPseudoReference;

/**
 * Class that represents the shared portion of an object 
 * in the static method area, i.e., its static fields.
 */
public final class Klass extends Objekt {
    private boolean initialized;

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
     * @param numOfStaticFields an {@code int}, the number of static fields.
     * @param fieldSignatures varargs of field {@link Signature}s, all the
     *        fields this object knows.
     */
    Klass(boolean symbolic, Calculator calc, KlassPseudoReference origin, HistoryPoint epoch, int numOfStaticFields, Signature... fieldSignatures) {
        super(symbolic, calc, null, origin, epoch, true, numOfStaticFields, fieldSignatures);
        this.initialized = false;
    }

    /**
     * Checks whether this {@link Klass} is initialized.
     * 
     * @return {@code true} iff this {@link Klass} is initialized.
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Sets this {@link Klass} to the
     * initialized status. After the 
     * invocation of this method an 
     * invocation to {@link #isInitialized()} 
     * will return {@code true}.
     */
    public void setInitialized() {
        this.initialized = true;
    }

    @Override
    public Klass clone() {
        final Klass o = (Klass) super.clone();
        o.fields = fieldsDeepCopy();

        return o;
    }
}
