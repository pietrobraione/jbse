package jbse.val;

/**
 * Class that represent a {@link ReferenceSymbolicAtomic} whose origin is not a root
 * (that is, is a member of an object transitively referred by a root). 
 */
public abstract class ReferenceSymbolicMember extends ReferenceSymbolicAtomic implements SymbolicMember {
    private final ReferenceSymbolic container;

    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param historyPoint the current {@link HistoryPoint}.
     */
    ReferenceSymbolicMember(ReferenceSymbolic container, int id, String staticType) {
    	super(id, staticType, container.historyPoint());
    	this.container = container;
    }

    @Override
    public final ReferenceSymbolic getContainer() {
        return this.container;
    }
}