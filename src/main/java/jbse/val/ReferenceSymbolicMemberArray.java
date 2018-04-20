package jbse.val;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is a slot
 * in an array. 
 */
public final class ReferenceSymbolicMemberArray extends ReferenceSymbolicMember implements SymbolicMemberArray {
    private final Primitive index;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer an array.
     * @param index a {@link Primitive}, the index of the slot in the 
     *        container array this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     */
    ReferenceSymbolicMemberArray(ReferenceSymbolic container, Primitive index, int id, String staticType) {
    	super(container, id, staticType);
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