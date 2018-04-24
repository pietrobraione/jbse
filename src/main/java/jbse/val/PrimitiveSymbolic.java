package jbse.val;

import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

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
     * @param historyPoint the current {@link HistoryPoint}.
     * @param calc a {@link Calculator}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    PrimitiveSymbolic(char type, HistoryPoint historyPoint, Calculator calc) throws InvalidTypeException {
    	super(type, calc);
        this.historyPoint = historyPoint;
    }
    
    /**
     * {@inheritDoc}
     */
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
     * For {@link PrimitiveSymbolic} values it will always throw {@link ValueDoesNotSupportNativeException}.
     */
    @Override
    public final Object getValueForNative() throws ValueDoesNotSupportNativeException {
        throw new ValueDoesNotSupportNativeException();
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