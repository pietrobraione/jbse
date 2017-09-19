package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Strategy for updating a state, i.e., for performing the 
 * operations that put in effect the bytecode semantics.
 * 
 * @author Pietro Braione
 *
 * @param <R> the type of the possible outcome of a previous decision.
 */
@FunctionalInterface
public interface StrategyUpdate<R> {
    /**
     * Updates a state, i.e., performs the 
     * operations that put in effect the bytecode semantics.
     * 
     * @param state the {@link State} to be updated.
     * @param alt the decision alternative that justifies {@code state}, 
     *        and gives the criterion on which the state shall be updated.
     * @throws DecisionException possibly raised if the update 
     *         action must simplify some symbolic array entries.
     * @throws ThreadStackEmptyException raised if the
     *         state has an empty stack during the update action.
     * @throws ClasspathException possibly raised if the classpath 
     *         does not contain the standard library, or if its 
     *         version is incompatible with JBSE.
     * @throws CannotManageStateException possibly raised if JBSE
     *         does not implement the bytecode semantics for the
     *         current execution state.
     * @throws DecisionException possibly raised by array-related
     *         operations (e.g., detecting unsatisfiable entries).
     * @throws ContradictionException possibly raised if the state 
     *         falsifies an assumption.
     * @throws FailureException possibly raised if the state falsifies
     *         an assertion.
     * @throws InterruptException whenever the execution of the current 
     *         bytecode for {@code state} must be prematurely ended now.
     */
    public void update(State state, R alt) 
    throws ThreadStackEmptyException, ClasspathException,
    CannotManageStateException, DecisionException, 
    ContradictionException, FailureException, InterruptException;
}