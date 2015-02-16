package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.InterruptException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Interface for all the Strategies for executing a bytecode.
 * 
 * @author Pietro Braione
 *
 */
public interface Algorithm {
    void exec(State state, ExecutionContext ctx) 
    throws CannotManageStateException, ClasspathException,  
    ContradictionException, DecisionException, 
    FailureException, ThreadStackEmptyException,
    InterruptException;
    default boolean someReferenceNotExpanded() { return false; }
    default String nonExpandedReferencesOrigins() { return null; }
    default String nonExpandedReferencesTypes() { return null; }
}
