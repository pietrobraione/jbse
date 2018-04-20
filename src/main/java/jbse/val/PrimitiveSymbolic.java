package jbse.val;

import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Class that represent a symbolic value. 
 */
public abstract class PrimitiveSymbolic extends Primitive implements Symbolic {
    /**
     * Constructor.
     * 
     * @param type the type of the represented value.
     * @param historyPoint a {@link String}, the current history point.
     * @param calc a {@link Calculator}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    PrimitiveSymbolic(char type, Calculator calc) throws InvalidTypeException {
    	super(type, calc);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getValue() {
    	return toString();
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