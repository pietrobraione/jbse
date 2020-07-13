package jbse.val;

/**
 * Class for representing all Java values.
 * 
 * @author unknown
 * @author Pietro Braione
 */
public abstract class Value {
    /** The type of this {@link Value}. */
    private final char type;

    /**
     * Constructor.
     * 
     * @param type a {@code char}, the type of this {@link Value}.
     */
    Value(char type) {
    	this.type = type;
    }

    /**
     * Returns the type of this {@link Value}.
     * 
     * @return a {@code char} signifying the type of this value.
     */
    public final char getType() {
		return this.type;
    }

    /**
     * Checks whether this {@link Value} is symbolic.
     * 
     * @return {@code true} if this value is symbolic, 
     *         {@code false} if this value is concrete.
     */
    public abstract boolean isSymbolic();
    
    /**
     * Accepts a {@link ValueVisitor}.
     * 
     * @param v a {@link ValueVisitor}.
     * @throws Exception whenever {@code v} throws an {@link Exception}.
     */
    public abstract void accept(ValueVisitor v) throws Exception;

    @Override
    public abstract String toString();
    
    @Override
    public abstract boolean equals(Object o);
    
    @Override
    public abstract int hashCode();
}
