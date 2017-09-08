package jbse.val;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidTypeException;

public final class SymbolFactory implements Cloneable {
    /** The {@link Calculator}. */
    private final Calculator calc;

	/** The next available identifier for a new reference-typed symbolic value. */
	private int nextIdRefSym;

	/** The next available identifier for a new primitive-typed symbolic value. */
	private int nextIdPrimSym;
	
	public SymbolFactory(Calculator calc) {
        this.calc = calc;
		this.nextIdRefSym = 0;
		this.nextIdPrimSym = 0;
	}
	
	public Value createSymbol(String staticType, MemoryPath origin) {
        try {
            final Value retVal;
            if (Type.isPrimitive(staticType)) {
                retVal = new PrimitiveSymbolic(this.getNextIdPrimitiveSymbolic(), staticType.charAt(0), origin, calc);
            } else {
                retVal = new ReferenceSymbolic(this.getNextIdReferenceSymbolic(), staticType, origin);
            }
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
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
