package jbse.val;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * The boolean value on the top of the information lattice of booleans.
 * It always have either {@code true} or {@code false} as value and 
 * no further assumption on it can be done. It is used to model 
 * nondeterminism.
 * 
 * @author Pietro Braione
 */
public final class Any extends Primitive {
	private Any(Calculator calc) throws InvalidTypeException {
		super(Type.BOOLEAN, calc);
	}
	
	public static Any make(Calculator calc) {
		try {
			return new Any(calc);
		} catch (InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
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
	public void accept(PrimitiveVisitor v) throws Exception { 
		v.visitAny(this);
	}

	@Override
	public boolean isSymbolic() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return "*";
	}
}
