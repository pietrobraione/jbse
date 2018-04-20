package jbse.val;


/**
 * Interface that must be implemented by all the {@link SymbolicAtomic}
 * values whose origin is a local variable in the root frame.
 * 
 * @author Pietro Braione
 *
 */
public interface SymbolicLocalVariable extends SymbolicAtomic {
    /**
     * Returns the name of the local variable.
     * 
     * @return a {@link String}.
     */
    String getVariableName();
}
