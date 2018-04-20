package jbse.val;

import static jbse.common.Type.INT;

import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicMember} whose origin is the
 * length of an array.
 */
public final class PrimitiveSymbolicMemberArrayLength extends PrimitiveSymbolicMember {
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer an array.
     * @param index a {@link Primitive}, the index of the slot in the 
     *        container array this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param type the type of the represented value.
     * @param calc a {@link Calculator}.
     * @throws InvalidTypeException (never).
     */
    PrimitiveSymbolicMemberArrayLength(ReferenceSymbolic container, int id, Calculator calc) throws InvalidTypeException {
    	super(container, id, INT, calc);
    }
    
    @Override
    public String asOriginString() {
        return this.getContainer().asOriginString() + ".length";
    }
}