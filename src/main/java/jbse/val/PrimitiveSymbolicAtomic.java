package jbse.val;

import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolic} atomic 
 * (non computed) value. 
 */
public abstract class PrimitiveSymbolicAtomic extends PrimitiveSymbolic implements SymbolicAtomic {    
    /** The identifier of this symbol. */
    private final int id;
    
    /** The hash code. */
    private final int hashCode;
    
    /** The string representation of this object. */
    private final String toString;
    
    /**
     * Constructor.
     * 
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param type the type of the represented value.
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws NullPointerException if {@code calc == null || historyPoint == null}.
     */
    PrimitiveSymbolicAtomic(int id, char type, HistoryPoint historyPoint, Calculator calc) throws InvalidTypeException {
    	super(type, historyPoint, calc);
    	this.id = id;
        
    	//calculates hashCode
    	final int prime = 89;
    	int result = 1;
    	result = prime * result + id;
    	this.hashCode = result;

    	//calculates toString
    	this.toString = "{V" + this.id + "}";
    }

    @Override
    public final int getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return this.hashCode;
    }
    
    @Override
    public final void accept(PrimitiveVisitor v) throws Exception {
        v.visitPrimitiveSymbolicAtomic(this);
    }

    /**
     * {@inheritDoc}
     * Two {@link PrimitiveSymbolic} values are equal iff they
     * have same identifier.
     */
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
        final PrimitiveSymbolicAtomic other = (PrimitiveSymbolicAtomic) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return this.toString;
    }
}