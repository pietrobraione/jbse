package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is a field
 * in an object (non array). 
 */
public final class ReferenceSymbolicMemberField extends ReferenceSymbolicMember implements SymbolicMemberField {
    private final String fieldName;
    private final String originString;
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must not refer an array.
     * @param fieldName a {@link String}, the name of the field in the 
     *        container object this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @throws InvalidTypeException  if {@code staticType} is not an array or instance
	 *         reference type.
     * @throws InvalidInputException if {@code staticType == null || fieldName == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberField(ReferenceSymbolic container, String fieldName, int id, String staticType) throws InvalidInputException, InvalidTypeException {
    	super(container, id, staticType);
    	if (fieldName == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberField with null fieldName.");
    	}
    	this.fieldName = fieldName;
    	this.originString = getContainer().asOriginString() + "." + this.fieldName;

    	//calculates hashCode
		final int prime = 3671;
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
		final ReferenceSymbolicMemberField other = (ReferenceSymbolicMemberField) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (!this.fieldName.equals(other.fieldName)) {
			return false;
		}
		return true;
	}
}