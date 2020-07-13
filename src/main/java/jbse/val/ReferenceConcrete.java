package jbse.val;

import jbse.common.Type;
import jbse.mem.Util;

public class ReferenceConcrete extends Reference {
    /** The position in the heap denoted by this {@link Reference}. */
    private final long pos;

    /**
     * Constructor returning a concrete reference to an object.
     * 
     * @param type a {@code char}, the type of the reference (see {@link Type}).
     * @param pos position of the object in the heap.
     */
    protected ReferenceConcrete(char type, long pos) {
        super(type);
        this.pos = pos;
    }

    /**
     * Constructor returning a concrete reference to an object.
     * 
     * @param pos position of the object in the heap.
     */
    public ReferenceConcrete(long pos) {
        super();
        this.pos = pos;
    }

    /**
     * Checks whether this {@link Reference} denotes {@code null}. 
     * 
     * @return {@code true} iff {@code this} denotes {@code null}.
     */
    public final boolean isNull() {
        return (this.pos == Util.POS_NULL);
    }

    /**
     * Returns the heap position that this reference denotes.
     * 
     * @return a {@code long}, the heap position.
     */
    public final long getHeapPosition() {
        return this.pos;
    }
    
    @Override
    public final void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceConcrete(this);
    }

    @Override
    public final boolean isSymbolic() {
        return false;
    }

    @Override
    public String toString() {
        return("Object[" + this.pos + "]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        final ReferenceConcrete r = (ReferenceConcrete) o;            
        return (this.pos == r.pos);
    }

    @Override
    public final int hashCode() {
        return (int) this.pos;
    }
}
