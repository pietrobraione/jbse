package jbse.val;

import static jbse.common.Type.INT;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicAtomic} whose origin is
 * the hash code of an object. 
 */
public final class PrimitiveSymbolicHashCode extends PrimitiveSymbolicAtomic {
    /** 
     * The container object can be null if this is the hash code
     * of a concrete object.
     */
    private final ReferenceSymbolic container;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container (symbolic) 
     *        object this hash code originates from, or {@code null} if this
     *        hash code is the hash code of an object not present in the
     *        initial state (i.e., a concrete object).
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param historyPoint the current {@link HistoryPoint} if {@code container == null}.
     *        In such case it must not be {@code null}.
     * @param calc a {@link Calculator}.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException if {@code container == null && historyPoint == null}.
     */
    PrimitiveSymbolicHashCode(ReferenceSymbolic container, int id, HistoryPoint historyPoint, Calculator calc) throws InvalidTypeException, InvalidInputException {
    	super(id, INT, (container == null ? historyPoint : container.historyPoint()), calc);
    	if (container == null && historyPoint == null) {
    		throw new InvalidInputException("Tried to construct a symbolic hash code with null container and historyPoint.");
    	}
    	this.container = container;
    }

    public ReferenceSymbolic getContainer() {
        return this.container;
    }

    @Override
    public String asOriginString() {
        return (this.container == null ? this.historyPoint().toString() : this.container.asOriginString()) + ".<identityHashCode>";
    }
}