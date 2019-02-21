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
    /** The string representation of this object. */
    private final String toString;

    /**
     * Constructor returning an uninitialized symbolic reference.
     * 
     * @param id an {@code int} identifying the reference univocally.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param historyPoint the current {@link HistoryPoint}.
     * @throws InvalidTypeException  if {@code staticType} is not an array or instance
     *         reference type.
     * @throws InvalidInputException if {@code staticType == null || historyPoint == null}.
     */
    ReferenceSymbolicAtomic(int id, String staticType, HistoryPoint historyPoint) throws InvalidInputException, InvalidTypeException {
        super(staticType, historyPoint);
        if (staticType == null) {
            throw new InvalidInputException("Attempted to build a ReferenceSymbolicAtomic with null static type.");
        }
        if (!isArray(staticType) && !isReference(staticType)) {
            throw new InvalidTypeException("Attempted to build a ReferenceSymbolicAtomic with static type " + staticType + " (neither array nor instance reference type).");
        }

        //calculates toString
        this.toString = "{R" + id + "}";
    }

    @Override
    public final String toString() {
        return this.toString;
    }
}
