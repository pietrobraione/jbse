package jbse.val;

import static jbse.common.Type.INT;

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
     * The history point of when this hash code was created.
     */
    private final HistoryPoint historyPoint;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this hash code originates from, or {@code null} if this
     *        hash code is the hash code of an object not present in the
     *        initial state.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param historyPoint the current {@link HistoryPoint} if {@code container == null}.
     * @param calc a {@link Calculator}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    PrimitiveSymbolicHashCode(ReferenceSymbolic container, int id, HistoryPoint historyPoint, Calculator calc) throws InvalidTypeException {
    	super(id, INT, calc);
    	this.container = container;
    	this.historyPoint = historyPoint;
    }

    public ReferenceSymbolic getContainer() {
        return this.container;
    }

    public HistoryPoint getHistoryPoint() {
        return this.historyPoint;
    }
    
    @Override
    public String asOriginString() {
        return this.container.asOriginString() + ".<hashCode>";
    }
}