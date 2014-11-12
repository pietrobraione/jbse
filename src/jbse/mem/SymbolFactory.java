package jbse.mem;

import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidTypeException;

class SymbolFactory implements Cloneable {
	/** The next available identifier for a new reference-typed symbolic value. */
	private int nextIdRefSym;

	/** The next available identifier for a new primitive-typed symbolic value. */
	private int nextIdPrimSym;

	SymbolFactory() {
		this.nextIdRefSym = 0;
		this.nextIdPrimSym = 0;
	}
	
	Value createSymbol(String descriptor, String origin, Calculator calc) {
		final Value retVal;
		if (Type.isPrimitive(descriptor)) {
			try {
				retVal = new PrimitiveSymbolic(this.getNextIdPrimitiveSymbolic(), descriptor.charAt(0), origin, calc);
			} catch (InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		} else {
			retVal = new ReferenceSymbolic(this.getNextIdReferenceSymbolic(), descriptor, origin);
		}
		return retVal;
	}
	
	private int getNextIdPrimitiveSymbolic() {
		final int retVal = this.nextIdPrimSym++;
		return retVal;
	}
	
	private int getNextIdReferenceSymbolic() {
		final int retVal = this.nextIdRefSym++;
		return retVal;
	}
	
	@Override
	public SymbolFactory clone() {
		final SymbolFactory o;
		try {
			o = (SymbolFactory) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		return o;
	}
}
