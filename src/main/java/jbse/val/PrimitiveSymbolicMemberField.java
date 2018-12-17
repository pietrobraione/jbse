package jbse.val;

import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicMember} whose origin is a field
 * in an object (non array). 
 */
public final class PrimitiveSymbolicMemberField extends PrimitiveSymbolicMember implements SymbolicMemberField {
    private final String fieldName;
    
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
     * @throws NullPointerException if {@code calc == null || container == null || fieldName == null}.
     */
    PrimitiveSymbolicMemberField(ReferenceSymbolic container, String fieldName, int id, char type, Calculator calc) throws InvalidTypeException {
    	super(container, id, type, calc);
    	if (fieldName == null) {
    		throw new NullPointerException("Tried to construct a symbolic object field member with null field name.");
    	}
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