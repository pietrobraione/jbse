package jbse.val;

import jbse.bc.ClassFile;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Klass;
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
        
	/**
	 * A Factory Method for creating symbolic values. The symbol
	 * has as origin a local variable in the root frame.
	 * 
	 * @param staticType a {@link String}, the static type of the
	 *        local variable from which the symbol originates.
	 * @param variableName a {@link String}, the name of the local 
	 *        variable in the root frame the symbol originates from.
	 * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
	 *         according to {@code staticType}.
	 */
        public Value createSymbolLocalVariable(String staticType, String variableName) {
        try {
            final Value retVal;
            if (Type.isPrimitive(staticType)) {
                retVal = new PrimitiveSymbolicLocalVariable(variableName, this.getNextIdPrimitiveSymbolic(), staticType.charAt(0), null, this.calc);
            } else {
                retVal = new ReferenceSymbolicLocalVariable(variableName, this.getNextIdReferenceSymbolic(), staticType, null);
            }
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        }
	
        /**
         * A Factory Method for creating symbolic values. The symbol
         * is a (pseudo)reference to a {@link Klass}.
         * 
         * @param classFile the {@link ClassFile} for the {@link Klass} to be referred.
         * @return a {@link KlassPseudoReference}.
         */
        public KlassPseudoReference createSymbolKlassPseudoReference(ClassFile classFile) {
            final KlassPseudoReference retVal = new KlassPseudoReference(classFile);
            return retVal;
        }
        
        /**
         * A Factory Method for creating symbolic values. The symbol
         * has as origin a field in an object (non array). 
         * 
         * @param staticType a {@link String}, the static type of the
         *        local variable from which the symbol originates.
         * @param container a {@link ReferenceSymbolic}, the container object
         *        the symbol originates from. It must not refer an array.
         * @param fieldName a {@link String}, the name of the field in the 
         *        container object the symbol originates from.
         * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
         *         according to {@code staticType}.
         */
	public Value createSymbolMemberField(String staticType, ReferenceSymbolic container, String fieldName) {
        try {
            final Value retVal;
            if (Type.isPrimitive(staticType)) {
                retVal = new PrimitiveSymbolicMemberField(container, fieldName, this.getNextIdPrimitiveSymbolic(), staticType.charAt(0), this.calc);
            } else {
                retVal = new ReferenceSymbolicMemberField(container, fieldName, this.getNextIdReferenceSymbolic(), staticType);
            }
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
	}
        
        /**
         * A Factory Method for creating symbolic values. The symbol
         * has as origin a slot in an array.  
         * 
         * @param staticType a {@link String}, the static type of the
         *        local variable from which the symbol originates.
         * @param container a {@link ReferenceSymbolic}, the container object
         *        the symbol originates from. It must refer an array.
         * @param index a {@link Primitive}, the index of the slot in the 
         *        container array this symbol originates from.
         * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
         *         according to {@code staticType}.
         */
        public Value createSymbolMemberArray(String staticType, ReferenceSymbolic container, Primitive index) {
        try {
            final Value retVal;
            if (Type.isPrimitive(staticType)) {
                retVal = new PrimitiveSymbolicMemberArray(container, index, this.getNextIdPrimitiveSymbolic(), staticType.charAt(0), this.calc);
            } else {
                retVal = new ReferenceSymbolicMemberArray(container, index, this.getNextIdReferenceSymbolic(), staticType);
            }
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        }
	
        /**
         * A Factory Method for creating symbolic values. The symbol
         * has as origin the length of an array.  
         * 
         * @param container a {@link ReferenceSymbolic}, the container object
         *        the symbol originates from. It must refer an array.
         * @return a {@link PrimitiveSymbolic}.
         */
        public PrimitiveSymbolic createSymbolMemberArrayLength(ReferenceSymbolic container) {
        try {
            final PrimitiveSymbolicMemberArrayLength retVal = new PrimitiveSymbolicMemberArrayLength(container, this.getNextIdPrimitiveSymbolic(), this.calc);
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        }
        
        /**
         * A Factory Method for creating symbolic values. The symbol
         * has as origin the hash code of an object (the object exists
         * in the initial heap).  
         * 
         * @param container a {@link ReferenceSymbolic}, the container object
         *        the symbol originates from.
         * @return a {@link PrimitiveSymbolic}.
         */
        public PrimitiveSymbolic createSymbolHashCode(ReferenceSymbolic container) {
        try {
            final PrimitiveSymbolicHashCode retVal = new PrimitiveSymbolicHashCode(container, this.getNextIdPrimitiveSymbolic(), null, this.calc);
            return retVal;
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        }
        
        /**
         * A Factory Method for creating symbolic values. The symbol
         * has as origin the hash code of an object (the object does
         * not exist in the initial heap).  
         * 
         * @param historyPoint the current {@link HistoryPoint}.
         * @return a {@link PrimitiveSymbolic}.
         */
        public PrimitiveSymbolic createSymbolHashCode(HistoryPoint historyPoint) {
        try {
            final PrimitiveSymbolicHashCode retVal = new PrimitiveSymbolicHashCode(null, this.getNextIdPrimitiveSymbolic(), historyPoint, this.calc);
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
