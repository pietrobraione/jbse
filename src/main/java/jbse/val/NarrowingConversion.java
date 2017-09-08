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
public final class NarrowingConversion extends Primitive {
	private final Primitive arg;
	private final String toString;
	private final int hashCode;

	private NarrowingConversion(char type, Calculator calc, Primitive arg) 
	throws InvalidOperandException, InvalidTypeException {
		super(type, calc);

		//checks on parameters
        if (arg == null) {
    		throw new InvalidOperandException("null operand in narrowing construction");
        }
		if (!Type.narrows(type, arg.getType())) {
			throw new InvalidTypeException("cannot narrow type " + arg.getType() + " to type " + type);
		}
		this.arg = arg;
		
        //calculates hashCode
		final int prime = 311;
		int result = 1;
		result = prime * result + arg.hashCode();
		result = prime * result + type;
		this.hashCode = result;

		//calculates toString
		this.toString = "NARROW-"+ this.getType() + "(" + arg.toString() + ")";
}
	
	public static NarrowingConversion make(char type, Calculator calc, Primitive arg) 
	throws InvalidOperandException, InvalidTypeException {
		return new NarrowingConversion(type, calc, arg);
	}
	
	public Primitive getArg() {
		return this.arg;
	}

	@Override
	public void accept(PrimitiveVisitor v) throws Exception {
		v.visitNarrowingConversion(this);
	}

	@Override
	public boolean surelyTrue() {
		return false;
	}

	@Override
	public boolean surelyFalse() {
		return false;
	}

	@Override
	public boolean isSymbolic() {
		return true;
	}

	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}

	@Override
	public String toString() {
		return this.toString;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NarrowingConversion other = (NarrowingConversion) obj;
		if (arg == null) {
			if (other.arg != null) {
				return false;
			}
		} else if (!arg.equals(other.arg)) {
			return false;
		}
		return true;
	}

}
