package jbse.val;


/**
 * Interface that must be implemented by all the {@link Symbolic}
 * values that are atomic (non computed).
 * 
 * @author Pietro Braione
 *
 */
public interface SymbolicAtomic extends Symbolic {
    /**
     * Returns the identifier of the symbol.
     * 
     * @return a {@code int}, the identifier 
     * of the symbol. Two different symbols 
     * can have same identifier only if they
     * belong to different classes. 
     */
    int getId();
}
