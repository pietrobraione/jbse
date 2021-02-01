package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicAtomic} whose origin is not a root
 * (that is, is a member of an object transitively referred by a root). 
 */
public abstract class ReferenceSymbolicMember extends ReferenceSymbolicAtomic implements SymbolicMember {
	/** 
	 * The {@link ReferenceSymbolic} to the container object 
	 * this symbol originates from. 
	 */
    private final ReferenceSymbolic container;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must not be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param genericSignatureType a {@link String}, the generic signature 
     *        type of the reference (taken from bytecode, its type erasure
     *        must be {@code staticType}).
     * @throws InvalidTypeException if {@code staticType} is not an array or instance
	 *         reference type.
     * @throws InvalidInputException if {@code staticType == null || genericSignatureType == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMember(ReferenceSymbolic container, int id, String staticType, String genericSignatureType) 
    throws InvalidInputException, InvalidTypeException {
    	super(id, staticType, genericSignatureType, container.historyPoint());
    	this.container = container;
    }
    
    @Override
    public final ReferenceSymbolic getContainer() {
        return this.container;
    }
    
    //these disambiguate the excessively complex hierarchy 
    
    @Override
    public ReferenceSymbolic root() {
    	return SymbolicMember.super.root();
    }
    
    @Override
    public boolean hasContainer(Symbolic r) {
    	return SymbolicMember.super.hasContainer(r);
    }
}