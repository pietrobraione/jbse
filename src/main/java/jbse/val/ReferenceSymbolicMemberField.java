package jbse.val;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is a field
 * in an object (non array). 
 */
public final class ReferenceSymbolicMemberField extends ReferenceSymbolicMember implements SymbolicMemberField {
    private final String fieldName;
    
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
     */
    ReferenceSymbolicMemberField(ReferenceSymbolic container, String fieldName, int id, String staticType) {
    	super(container, id, staticType);
    	this.fieldName = fieldName;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }
    
    @Override
    public String asOriginString() {
        return this.getContainer().asOriginString() + "." + this.fieldName;
    }
}