package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicMember} whose origin is a field
 * in an object (non array). 
 */
public final class PrimitiveSymbolicMemberField extends PrimitiveSymbolicMember implements SymbolicMemberField {
    private final String fieldName;
    private final String originString;
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from.  It must not be {@code null} and 
     *        it must not refer an array.
     * @param fieldName a {@link String}, the name of the field in the 
     *        container object this symbol originates from. It must not 
     *        be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param type the type of the represented value.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code calc == null || fieldName == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    PrimitiveSymbolicMemberField(ReferenceSymbolic container, String fieldName, int id, char type, Calculator calc) throws InvalidTypeException, InvalidInputException {
    	super(container, id, type, calc);
    	if (fieldName == null) {
    		throw new InvalidInputException("Attempted to construct a symbolic object field member with null field name.");
    	}
    	this.fieldName = fieldName;
    	this.originString = getContainer().asOriginString() + "." + this.fieldName;

    	//calculates hashCode
		final int prime = 7211;
		int result = 1;
		result = prime * result + getContainer().hashCode();
		result = prime * result + fieldName.hashCode();
		this.hashCode = result;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
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
		final PrimitiveSymbolicMemberField other = (PrimitiveSymbolicMemberField) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (!this.fieldName.equals(other.fieldName)) {
			return false;
		}
		return true;
	}
}