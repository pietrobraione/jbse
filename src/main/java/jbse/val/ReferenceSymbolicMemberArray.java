package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is a slot
 * in an array. 
 */
public final class ReferenceSymbolicMemberArray extends ReferenceSymbolicMember implements SymbolicMemberArray {
	/** The index of the slot in the container array this symbol originates from. */
    private final Primitive index;
    
    /** The origin String representation of this object. */
    private final String asOriginString;
    
    /** The hash code of this object. */
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer an array.
     * @param index a {@link Primitive}, the index of the slot in the 
     *        container array this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param genericSignatureType a {@link String}, the generic signature 
     *        type of the reference (taken from bytecode, its type erasure
     *        must be {@code staticType}).
     * @throws InvalidTypeException if {@code staticType} is not an array or instance
	 *         reference type.
     * @throws InvalidInputException if {@code staticType == null || genericSignatureType == null || index == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberArray(ReferenceSymbolic container, Primitive index, int id, String staticType, String genericSignatureType) 
    throws InvalidInputException, InvalidTypeException {
    	super(container, id, staticType, genericSignatureType);
    	if (index == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberArray with null index.");
    	}
    	
    	this.index = index;
    	this.asOriginString = getContainer().asOriginString() + "[" + (this.index.isSymbolic() ? ((Symbolic) this.index).asOriginString() : this.index.toString()) + "]";

    	//calculates hashCode
		final int prime = 677;
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
        return this.asOriginString;
    }
    
    @Override
    public void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceSymbolicMemberArray(this);
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
		final ReferenceSymbolicMemberArray other = (ReferenceSymbolicMemberArray) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (!this.index.equals(other.index)) {
			return false;
		}
		return true;
	}
}