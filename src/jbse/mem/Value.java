package jbse.mem;

import jbse.exc.mem.ValueDoesNotSupportNativeException;

/**
 * Class for representing Java values in memory, either primitive 
 * or not, either concrete or symbolic.
 * 
 * @author unknown
 * @author Pietro Braione
 */
public abstract class Value implements Cloneable {
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
     * Checks whether this {@link Value} is symbolic.
     * 
     * @return {@code true} if this value is symbolic, 
     *         {@code false} if this value is concrete.
     */
    public abstract boolean isSymbolic();

    /**
     * Returns a Java object (if possible) denoting the same value as 
     * this object, and that can be passed as parameter to metacircular 
     * native method calls.
     * 
     * @return an {@link Object}.
     * @throws ValueDoesNotSupportNativeException if this {@link Value} 
     *         cannot suitably be represented at meta level.
     */
    public abstract Object getValueForNative() throws ValueDoesNotSupportNativeException;
    
    /**
     * Returns the type of this {@link Value}.
     * 
     * @return a {@code char} signifying the type of this value.
     */
    public char getType() {
		return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value clone() {
    	final Value o;
    	try {
    		o = (Value) super.clone();
    	} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
    	}
    	return o;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String toString();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals(Object o);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();
}
