package jbse.val;


/**
 * Interface that must be implemented by all the {@link SymbolicAtomic}
 * values whose origin is not a root
 * (that is, is a member of an object transitively referred by a root).
 * 
 * @author Pietro Braione
 *
 */
public interface SymbolicMember extends SymbolicAtomic {
    /**
     * Returns the origin of the container where the 
     * symbol was defined.
     * 
     * @return a {@link ReferenceSymbolic} of the object
     *         this symbol was originally a member. 
     */
    ReferenceSymbolic getContainer();

    @Override
    default ReferenceSymbolic root() {
    	return getContainer().root();
    }
    
    @Override
    default boolean hasContainer(Symbolic s) {
		if (s == null) {
			throw new NullPointerException();
		}
		return (s.equals(this) || getContainer().hasContainer(s));
    }
}
