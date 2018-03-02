package jbse.val;

import jbse.common.Type;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Class representing the values resulting from a widening
 * conversion between numeric primitive types.
 * 
 * @author Pietro Braione
 *
 */
public final class WideningConversion extends Conversion {

	protected WideningConversion(char type, Calculator calc, Primitive arg) throws InvalidOperandException, InvalidTypeException {
		super(type, calc, arg);
	}

	public static WideningConversion make(char type, Calculator calc, Primitive arg)
	throws InvalidOperandException, InvalidTypeException {
		return new WideningConversion(type, calc, arg);
	}

	@Override
	protected String conversionAsString() {
		return "widen";
	}

	@Override
	protected int getPrime() {
		return 281;
	}

	public Primitive getArg() {
		return this.arg;
	}

	@Override
	public void accept(PrimitiveVisitor v) throws Exception {
		v.visitWideningConversion(this);
	}
}
