package jbse.val;

import jbse.common.Type;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Class representing the values resulting from a narrowing
 * conversion between numeric primitive types.
 * 
 * @author Pietro Braione
 *
 */
public final class NarrowingConversion extends Conversion {

	private NarrowingConversion(char type, Calculator calc, Primitive arg)
			throws InvalidOperandException, InvalidTypeException {
		super(type, calc, arg);
	}

	@Override
	protected String conversionAsString() {
		return "narrow";
	}

	@Override
	protected int getPrime() {
		return 311;
	}

	public static NarrowingConversion make(char type, Calculator calc, Primitive arg) 
	throws InvalidOperandException, InvalidTypeException {
		return new NarrowingConversion(type, calc, arg);
	}

	@Override
	public void accept(PrimitiveVisitor v) throws Exception {
		v.visitNarrowingConversion(this);
	}
}
