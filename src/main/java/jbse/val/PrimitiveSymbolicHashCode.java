package jbse.val;

import static jbse.common.Type.INT;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicAtomic} whose origin is
 * the hash code of an object. 
 */
public final class PrimitiveSymbolicHashCode extends PrimitiveSymbolicAtomic {
	//note that this class is not a superclass of PrimitiveSymbolicMember because 
	//its object can have container == null, a thing that is forbidden by PrimitiveSymbolicMember.
	
    /** 
     * The container object can be null if this is the hash code
     * of a concrete object.
     */
    private final ReferenceSymbolic container;
    
	/** The hash code of this object. */
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container (symbolic) 
     *        object this hash code originates from, or {@code null} if this
     *        hash code is the hash code of an object not present in the
     *        initial state (i.e., a concrete object).
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param historyPoint the current {@link HistoryPoint} if {@code container == null}.
     *        In such case it must not be {@code null}.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException if {@code container == null && historyPoint == null}.
     */
    PrimitiveSymbolicHashCode(ReferenceSymbolic container, int id, HistoryPoint historyPoint) 
    throws InvalidTypeException, InvalidInputException {
    	super(id, INT, (container == null ? historyPoint : container.historyPoint()));
    	this.container = container;
    	
		//calculates hashCode
		final int prime = 1123;
		int result = 1;
		result = prime * result + ((this.container == null) ? 0 : container.hashCode());
		result = prime * result + ((this.container == null) ? historyPoint().hashCode() : 0);
		this.hashCode = result;
    }

    public ReferenceSymbolic getContainer() {
        return this.container;
    }
    
    @Override
    public Symbolic root() {
    	return (this.container == null ? this : this.container.root());
    }
    
    @Override
    public boolean hasContainer(Symbolic s) {
		if (s == null) {
			throw new NullPointerException();
		}
		return (s.equals(this) || (this.container != null && this.container.hasContainer(s)));
    }

    @Override
    public String asOriginString() {
        return (this.container == null ? historyPoint().toString() : this.container.asOriginString()) + ".<identityHashCode>";
    }

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PrimitiveSymbolicHashCode other = (PrimitiveSymbolicHashCode) obj;
		if (this.container == null) {
			if (other.container != null) {
				return false;
			}
		} else if (!this.container.equals(other.container)) {
			return false;
		}
		if (historyPoint() == null) {
			if (other.historyPoint() != null) {
				return false;
			}
		} else if (!historyPoint().equals(other.historyPoint())) {
			return false;
		}
		return true;
	}
}