package jbse.val;

import static jbse.common.Type.isPrimitive;

import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a primitive type value of any kind.
 */
public abstract class Primitive extends Value implements Cloneable {
    /**
     * Constructor. 
     * 
     * @param type a {@code char}, the type of this value.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    Primitive(char type) throws InvalidTypeException {
        super(type);
        if (!isPrimitive(type)) {
            throw new InvalidTypeException(type + " is not a primitive type");
        }
    }

    /**
     * Accepts a {@link PrimitiveVisitor}.
     * 
     * @param v a {@link PrimitiveVisitor}.
     * @throws Exception whenever {@code v} throws an {@link Exception}.
     */
    public abstract void accept(PrimitiveVisitor v) throws Exception;

    /**
     * Checks whether this value denotes the primitive true value.
     *  
     * @return {@code true} iff the value denotes the concrete primitive 
     *         true value.
     *         Note that symbolic {@link Primitive}s do not denote 
     *         the true or the false value.
     */
    public abstract boolean surelyTrue();

    /**
     * Checks whether this value is the primitive false value.
     *  
     * @return {@code true} iff the value denotes the concrete primitive 
     *         false value.
     *         Note that symbolic {@link Primitive}s do not denote 
     *         the true or the false value.
     */
    public abstract boolean surelyFalse();
}