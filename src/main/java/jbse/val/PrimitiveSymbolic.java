package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a symbolic value. 
 */
public abstract class PrimitiveSymbolic extends Primitive implements Symbolic {
    /** The creation history point of this symbol. */
    private final HistoryPoint historyPoint;

    /**
     * Constructor.
     * 
     * @param type the type of the represented value.
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code calc == null || historyPoint == null}.
     */
    PrimitiveSymbolic(char type, HistoryPoint historyPoint, Calculator calc) 
    throws InvalidTypeException, InvalidInputException {
    	super(type, calc);
    	if (historyPoint == null) {
    		throw new InvalidInputException("Attempted the creation of a PrimitiveSymbolic with null history point.");
    	}
        this.historyPoint = historyPoint;
    }

    @Override
    public final String getValue() {
    	return toString();
    }
    
    @Override
    public final HistoryPoint historyPoint() {
        return this.historyPoint;
    }

    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will always return {@code true}.
     */
    @Override
    public final boolean isSymbolic() {
        return true;
    }

    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will return {@code false}.
     */
    @Override
    public final boolean surelyTrue() {
        return false;
    }

    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will return {@code false}.
     */
    @Override
    public final boolean surelyFalse() {
        return false;
    }
}