package jbse.val;

import static jbse.common.Type.isArray;
import static jbse.common.Type.isReference;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolic} atomic 
 * (non computed) value.
 * 
 * @author Pietro Braione
 */
public abstract class ReferenceSymbolicAtomic extends ReferenceSymbolic implements SymbolicAtomic {
    /** The String representation of this object. */
    private final String toString;

    /** The generic signature type of the reference. */
    private final String genericSignatureType;

    /**
     * Constructor returning an uninitialized symbolic reference.
     * 
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode). 
     * @param genericSignatureType a {@link String}, the generic signature 
     *        type of the reference (taken from bytecode). It can 
     *        be {@code null}, in which case {@code staticType} will be
     *        used as generic signature type.
     * @param historyPoint the current {@link HistoryPoint}.
     * @throws InvalidTypeException if {@code staticType} is not an array or instance
     *         reference type.
     * @throws InvalidInputException if {@code staticType == null || genericSignatureType == null || historyPoint == null}.
     */
    ReferenceSymbolicAtomic(int id, String staticType, String genericSignatureType, HistoryPoint historyPoint) throws InvalidInputException, InvalidTypeException {
        super(staticType, historyPoint);
        if (staticType == null) {
            throw new InvalidInputException("Attempted to build a ReferenceSymbolicAtomic with null static type.");
        }
        if (!isArray(staticType) && !isReference(staticType)) {
            throw new InvalidTypeException("Attempted to build a ReferenceSymbolicAtomic with static type " + staticType + " (neither array nor instance reference type).");
        }
        this.genericSignatureType = genericSignatureType == null ? staticType : genericSignatureType;

        //calculates toString
        this.toString = "{R" + id + "}";
    }

    @Override
    public final String getGenericSignatureType() {
    	return this.genericSignatureType;
    }
    
    @Override
    public final String toString() {
        return this.toString;
    }
}
