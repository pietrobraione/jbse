package jbse.mem;

import jbse.Type;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.ValueDoesNotSupportNativeException;

/**
 * The boolean value on the top of the information lattice of booleans.
 * It always have either {@code true} or {@code false} as value and 
 * no further assumption on it can be done. It is used to model 
 * nondeterminism.
 * 
 * @author Pietro Braione
 */
public class Any extends Primitive {
	Any(Calculator calc) throws InvalidTypeException {
		super(Type.BOOLEAN, calc);
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
		return false;
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
