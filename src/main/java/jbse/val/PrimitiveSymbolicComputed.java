package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolic} value computed
 * from a set of other {@link Value}s. 
 */
public abstract class PrimitiveSymbolicComputed extends PrimitiveSymbolic {    
    /**
     * Constructor.
     * 
     * @param type the type of the represented value.
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code calc == null || historyPoint == null}.
     */
    PrimitiveSymbolicComputed(char type, HistoryPoint historyPoint, Calculator calc) 
    throws InvalidTypeException, InvalidInputException {
    	super(type, historyPoint, calc);
    }
    
    /**
     * Returns a new {@link PrimitiveSymbolicComputed} built by 
     * substitution of a subterm with another one.
     * 
     * @param from a {@link Primitive}, to be replaced. 
     * @param to a {@link Primitive}, replacing {@code from}.
     * @return the {@link Primitive} obtained by replacing 
     *         every occurrence of {@code from} in {@code this}
     *         with {@code to}.
     * @throws InvalidOperandException if {@code from} or {@code to}
     *         is {@code null}.
     * @throws InvalidTypeException if {@code from} and {@code to}
     *         have different type. 
     */
    public final Primitive replace(Primitive from, Primitive to)
    throws InvalidOperandException, InvalidTypeException {
        if (from == null || to == null) {
            throw new InvalidOperandException("one parameter of replace is null");
        }
        if (from.getType() != to.getType()) {
            throw new InvalidTypeException("cannot replace a primitive with type " + from.getType() + " with one with type " + to.getType());
        }
        if (from.equals(to)) {
            return this;
        }
        final Primitive retVal = doReplace(from, to);
        return retVal;
    }

    /**
     * Implements {@link #replace(Primitive, Primitive)}.
     * 
     * @param from a {@link Primitive}, to be replaced. It must
     *        not be {@code null}.
     * @param to a {@link Primitive}, replacing {@code from}.
     *        It must not be {@code null} and have same type as
     *        {@code from}.
     * @return the {@link Primitive} obtained by replacing 
     *         every occurrence of {@code from} in {@code this}
     *         with {@code to}.
     */
    public abstract Primitive doReplace(Primitive from, Primitive to);
}