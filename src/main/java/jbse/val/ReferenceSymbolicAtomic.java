package jbse.val;

/**
 * Class that represent a {@link ReferenceSymbolic} atomic 
 * (non computed) value.
 * 
 * @author Pietro Braione
 */
public abstract class ReferenceSymbolicAtomic extends ReferenceSymbolic implements SymbolicAtomic {
    /** An identifier for the value, in order to track lazy initialization. */
    private final int id;

    /** The hash code. */
    private final int hashCode;

    /** The string representation of this object. */
    private final String toString;

    /**
     * Constructor returning an uninitialized symbolic reference.
     * 
     * @param id an {@code int} identifying the reference univocally.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     */
    ReferenceSymbolicAtomic(int id, String staticType) {
        super(staticType);
        this.id = id;

        //calculates hashCode
        final int prime = 22901;
        int result = 1;
        result = prime * result + id;
        this.hashCode = result;

        //calculates toString
        this.toString = "{R" + this.id + "}";
    }

    @Override
    public final int getId() {
        return this.id;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReferenceSymbolicAtomic other = (ReferenceSymbolicAtomic) obj;
        return (this.id == other.id);
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public final String toString() {
        return this.toString;
    }
}
