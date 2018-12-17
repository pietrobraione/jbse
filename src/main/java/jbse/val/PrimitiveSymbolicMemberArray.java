package jbse.val;

import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicMember} whose origin is a slot
 * in an array. 
 */
public final class PrimitiveSymbolicMemberArray extends PrimitiveSymbolicMember implements SymbolicMemberArray {
    private final Primitive index;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from.  It must not be {@code null} and 
     *        it must refer an array.
     * @param index a {@link Primitive}, the index of the slot in the 
     *        container array this symbol originates from.  It must not be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param type the type of the represented value.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws NullPointerException if {@code calc == null || container == null || index == null}.
     */
    PrimitiveSymbolicMemberArray(ReferenceSymbolic container, Primitive index, int id, char type, Calculator calc) throws InvalidTypeException {
    	super(container, id, type, calc);
    	if (index == null) {
    		throw new NullPointerException("Tried to construct a symbolic array member with null array index.");
    	}
    	this.index = index;
    }

    @Override
    public Primitive getIndex() {
        return this.index;
    }
    
    @Override
    public String asOriginString() {
        return this.getContainer().asOriginString() + "[" + (this.index.isSymbolic() ? ((Symbolic) this.index).asOriginString() : this.index.toString()) + "]";
    }
}