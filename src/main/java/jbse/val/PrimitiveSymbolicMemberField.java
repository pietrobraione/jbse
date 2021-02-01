package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicMember} whose origin is a field
 * in an object (non array). 
 */
public final class PrimitiveSymbolicMemberField extends PrimitiveSymbolicMember implements SymbolicMemberField {
    private final String fieldName;
    private final String fieldClass;
    private final String asOriginString;
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
     * @param fieldClass a {@link String}, the name of the class where the 
     *        field is declared. It must not be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param type the type of the represented value.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code fieldName == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    PrimitiveSymbolicMemberField(ReferenceSymbolic container, String fieldName, String fieldClass, int id, char type) throws InvalidTypeException, InvalidInputException {
        super(container, id, type);
        if (fieldName == null || fieldClass == null) {
            throw new InvalidInputException("Attempted to construct a symbolic object field member with null fieldName or fieldClass.");
        }
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        this.asOriginString = getContainer().asOriginString() + "." + this.fieldClass + ":" + this.fieldName;

        //calculates hashCode
        final int prime = 7211;
        int result = 1;
        result = prime * result + getContainer().hashCode();
        result = prime * result + fieldName.hashCode();
        result = prime * result + fieldClass.hashCode();
        this.hashCode = result;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }
    
    @Override
    public String getFieldClass() {
        return this.fieldClass;
    }

    @Override
    public String asOriginString() {
        return this.asOriginString;
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
        if (!this.fieldClass.equals(other.fieldClass)) {
            return false;
        }
        return true;
    }
}