package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicAtomic} whose origin is not a root
 * (that is, is a member of an object transitively referred by a root). 
 */
public abstract class PrimitiveSymbolicMember extends PrimitiveSymbolicAtomic implements SymbolicMember {
    private final ReferenceSymbolic container;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must not be {@code null}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param type the type of the represented value.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException never.
     * @throws NullPointerException if {@code container == null}.
     */
    PrimitiveSymbolicMember(ReferenceSymbolic container, int id, char type) 
    throws InvalidTypeException, InvalidInputException {
    	super(id, type, container.historyPoint());
    	this.container = container;
    }

    @Override
    public final ReferenceSymbolic getContainer() {
        return this.container;
    }
}