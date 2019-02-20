package jbse.val;

import jbse.bc.ClassFile;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.val.exc.InvalidTypeException;

/**
 * A SymbolFactory creates {@link Symbolic} values for all the possible
 * origin sources of symbols.
 * 
 * @author Pietro Braione
 */
public final class SymbolFactory implements Cloneable {
    /** The {@link Calculator}. */
    private final Calculator calc;

    /** The next available identifier for a new reference-typed symbolic value. */
    private int nextIdRefSym;

    /** The next available identifier for a new primitive-typed symbolic value. */
    private int nextIdPrimSym;

    public SymbolFactory(Calculator calc) throws InvalidInputException {
        if (calc == null) {
            throw new InvalidInputException("Attempted creation of a SymbolFactory with null calc.");
        }
        this.calc = calc;
        this.nextIdRefSym = 0;
        this.nextIdPrimSym = 0;
    }

    /**
     * A Factory Method for creating symbolic values. The symbol
     * has as origin a local variable in the current frame.
     * 
     * @param historyPoint the {@link HistoryPoint} of the symbol.
     * @param staticType a {@link String}, the static type of the
     *        local variable from which the symbol originates.
     * @param variableName a {@link String}, the name of the local 
     *        variable in the root frame the symbol originates from.
     * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
     *         according to {@code staticType}.
     * @throws InvalidTypeException if {@code staticType} is not a valid type.
     * @throws InvalidInputException if {@code variableName == null || staticType == null || historyPoint == null}.
     */
    public Symbolic createSymbolLocalVariable(HistoryPoint historyPoint, String staticType, String variableName) throws InvalidTypeException, InvalidInputException {
        final Symbolic retVal;
        if (Type.isPrimitive(staticType)) {
            retVal = new PrimitiveSymbolicLocalVariable(variableName, getNextIdPrimitiveSymbolic(), staticType.charAt(0), historyPoint, this.calc);
        } else {
            retVal = new ReferenceSymbolicLocalVariable(variableName, getNextIdReferenceSymbolic(), staticType, historyPoint);
        }
        return retVal;
    }

    /**
     * A Factory Method for creating symbolic values. The symbol
     * is a (pseudo)reference to a {@link Klass}.
     * 
     * @param historyPoint the current {@link HistoryPoint}.
     * @param classFile the {@link ClassFile} for the {@link Klass} to be referred.
     * @return a {@link KlassPseudoReference}.
     * @throws InvalidInputException if {@code historyPoint == null || classFile == null}.
     */
    public KlassPseudoReference createSymbolKlassPseudoReference(HistoryPoint historyPoint, ClassFile classFile) throws InvalidInputException {
        final KlassPseudoReference retVal = new KlassPseudoReference(classFile, historyPoint);
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
     *        container object the symbol originates from. It must not be {@code null}.
     * @param fieldClass a {@link String}, the name of the class where the 
     *        field is declared. It must not be {@code null}.
     * @return a {@link PrimitiveSymbolic} or a {@link ReferenceSymbolic}
     *         according to {@code staticType}.
     * @throws InvalidTypeException if {@code staticType} is not a valid type.
     * @throws InvalidInputException if {@code fieldName == null || staticType == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    public Symbolic createSymbolMemberField(String staticType, ReferenceSymbolic container, String fieldName, String fieldClass)
    throws InvalidTypeException, InvalidInputException {
        final Symbolic retVal;
        if (Type.isPrimitive(staticType)) {
            retVal = new PrimitiveSymbolicMemberField(container, fieldName, fieldClass, getNextIdPrimitiveSymbolic(), staticType.charAt(0), this.calc);
        } else {
            retVal = new ReferenceSymbolicMemberField(container, fieldName, fieldClass, getNextIdReferenceSymbolic(), staticType);
        }
        return retVal;
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
     * @throws InvalidTypeException if {@code staticType} is not a valid type.
     * @throws InvalidInputException if {@code index == null || staticType == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    public Symbolic createSymbolMemberArray(String staticType, ReferenceSymbolic container, Primitive index) throws InvalidTypeException, InvalidInputException {
        final Symbolic retVal;
        if (Type.isPrimitive(staticType)) {
            retVal = new PrimitiveSymbolicMemberArray(container, index, getNextIdPrimitiveSymbolic(), staticType.charAt(0), this.calc);
        } else {
            retVal = new ReferenceSymbolicMemberArray(container, index, getNextIdReferenceSymbolic(), staticType);
        }
        return retVal;
    }

    /**
     * A Factory Method for creating symbolic values. The symbol
     * has as origin the length of an array.  
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        the symbol originates from. It must refer an array.
     * @return a {@link PrimitiveSymbolic}.
     * @throws NullPointerException if {@code container == null}.
     */
    public PrimitiveSymbolic createSymbolMemberArrayLength(ReferenceSymbolic container) {
        try {
            final PrimitiveSymbolicMemberArrayLength retVal = new PrimitiveSymbolicMemberArrayLength(container, getNextIdPrimitiveSymbolic(), this.calc);
            return retVal;
        } catch (InvalidInputException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * A Factory Method for creating symbolic values. The symbol
     * has as origin the identity hash code of a symbolic object.  
     * 
     * @param object a symbolic  {@link Objekt}, the object whose identity hash 
     *        code is this symbol. It must refer an instance or an array.
     * @return a {@link PrimitiveSymbolic}.
     * @throws InvalidInputException if {@code object == null}, or {@code object} has
     *         both its origin and its history point set to {@code null} (note that in 
     *         such case {@code object} is ill-formed).
     */
    public PrimitiveSymbolic createSymbolIdentityHashCode(Objekt object) throws InvalidInputException {
        if (object == null) {
            throw new InvalidInputException("Attempted the creation of an identity hash code by invoking " + this.getClass().getName() + ".createSymbolIdentityHashCode with null object.");
        }
        try {
            final PrimitiveSymbolicHashCode retVal = new PrimitiveSymbolicHashCode(object.getOrigin(), this.getNextIdPrimitiveSymbolic(), object.historyPoint(), this.calc);
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
            throw new UnexpectedInternalException(e);
        }
        return o;
    }
}
