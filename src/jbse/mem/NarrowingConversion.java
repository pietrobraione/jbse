package jbse.mem;

import jbse.Type;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.ValueDoesNotSupportNativeException;

/**
 * Class representing the values resulting from a narrowing
 * conversion between numeric primitive types.
 * 
 * @author Pietro Braione
 *
 */
public class NarrowingConversion extends Primitive {
	private final Primitive arg;
	private final String toString;
	private final int hashCode;

	private NarrowingConversion(char type, Calculator calc, Primitive arg) throws InvalidTypeException {
		super(type, calc);
		this.arg = arg;
		this.toString = "NARROW-"+ this.getType() + "(" + arg.toString() + ")";
		final int prime = 311;
		int result = 1;
		result = prime * result + ((arg == null) ? 0 : arg.hashCode());
		result = prime * result + type;
		this.hashCode = result;
	}
	
	public static NarrowingConversion make(char type, Calculator calc, Primitive arg) 
	throws InvalidOperandException, InvalidTypeException {
		if (arg == null) {
			throw new InvalidOperandException("arg of narrowing is null");
		}
		if (!Type.narrows(type, arg.getType())) {
			throw new InvalidTypeException("cannot narrow type " + arg.getType() + " to type " + type);
		}
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
