package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is a field
 * in an object (non array). 
 */
public final class ReferenceSymbolicMemberField extends ReferenceSymbolicMember implements SymbolicMemberField {
	/** 
	 * The name of the field in the container object 
	 * this symbol originates from.
     */
    private final String fieldName;
    
    /** 
     * The name of the class where the field is declared. 
     * Necessary to disambiguate private fields with same 
     * name declared in different (super)classes. 
     */
    private final String fieldClass;
    
    /** The origin String representation of this object. */
    private final String asOriginString;
    
    /** The hash code of this object. */
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must not refer an array.
     * @param fieldName a {@link String}, the name of the field in the 
     *        container object this symbol originates from. It must not be {@code null}.
     * @param fieldClass a {@link String}, the name of the class where the 
     *        field is declared. It must not be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param genericSignatureType a {@link String}, the generic signature 
     *        type of the reference (taken from bytecode, its type erasure
     *        must be {@code staticType}).
     * @throws InvalidTypeException  if {@code staticType} is not an array or instance
     *         reference type.
     * @throws InvalidInputException if {@code staticType == null || genericSignatureType == null || fieldName == null || fieldClass == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberField(ReferenceSymbolic container, String fieldName, String fieldClass, int id, String staticType, String genericSignatureType) 
    throws InvalidInputException, InvalidTypeException {
        super(container, id, staticType, genericSignatureType);
        if (fieldName == null || fieldClass == null) {
            throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberField with null fieldName or fieldClass.");
        }
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        this.asOriginString = getContainer().asOriginString() + "." + this.fieldClass + ":" + this.fieldName;

        //calculates hashCode
        final int prime = 3671;
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
    public void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceSymbolicMemberField(this);
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
        if (!this.fieldClass.equals(other.fieldClass)) {
            return false;
        }
        return true;
    }
}