package jbse.algo;

import jbse.exc.algo.CannotManageStateException;
import jbse.exc.dec.DecisionException;
import jbse.exc.jvm.FailureException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

/**
 * Interface for all the Strategies for executing a bytecode.
 * 
 * @author Pietro Braione
 *
 */
public interface Algorithm {
    void exec(State state, ExecutionContext ctx) 
    throws CannotManageStateException, ThreadStackEmptyException, 
    OperandStackEmptyException, ContradictionException, 
    DecisionException, FailureException;
    default boolean someReferenceNotExpanded() { return false; }
    default String nonExpandedReferencesOrigins() { return null; }
    default String nonExpandedReferencesTypes() { return null; }
}
