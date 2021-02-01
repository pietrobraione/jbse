package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicMember} whose origin is a slot
 * in an array. 
 */
public final class PrimitiveSymbolicMemberArray extends PrimitiveSymbolicMember implements SymbolicMemberArray {
    private final Primitive index;
    private final String originString;
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from.  It must not be {@code null} and 
     *        it must refer an array.
     * @param index a {@link Primitive}, the index of the slot in the 
     *        container array this symbol originates from.  It must not be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param type the type of the represented value.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code container == null || index == null}.
     */
    PrimitiveSymbolicMemberArray(ReferenceSymbolic container, Primitive index, int id, char type) throws InvalidTypeException, InvalidInputException {
    	super(container, id, type);
    	if (index == null) {
    		throw new InvalidInputException("Attempted to construct a symbolic array member with null array index.");
    	}
    	
    	this.index = index;
    	this.originString = getContainer().asOriginString() + "[" + (this.index.isSymbolic() ? ((Symbolic) this.index).asOriginString() : this.index.toString()) + "]";

    	//calculates hashCode
		final int prime = 2003;
		int result = 1;
		result = prime * result + getContainer().hashCode();
		result = prime * result + index.hashCode();
		this.hashCode = result;
    }

    @Override
    public Primitive getIndex() {
        return this.index;
    }
    
    @Override
    public String asOriginString() {
        return this.originString;
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
		final PrimitiveSymbolicMemberArray other = (PrimitiveSymbolicMemberArray) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (!this.index.equals(other.index)) {
			return false;
		}
		return true;
	}
}