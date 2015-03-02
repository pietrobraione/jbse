package jbse.algo;

import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Strategy for updating a state, i.e., for completing its 
 * bytecode semantics after a refinement.
 * 
 * @author Pietro Braione
 *
 * @param <R> the type of the possible outcome of a previous decision.
 */
@FunctionalInterface
interface StrategyUpdate<R> {
	/**
	 * Updates a state, i.e., completes its current bytecode's 
	 * semantics after a refinement.
	 * 
	 * @param s the {@link State} to be updated.
	 * @param r the outcome of a previous decision, which establishes 
	 *          the criterion on which the state shall be updated.
	 * @throws DecisionException possibly raised when the update 
     *         action must simplify some symbolic array entries.
	 * @throws ThreadStackEmptyException possibly raised if the
	 *         state has an empty stack during the update action.
	 */
	public void update(State s, R r) 
	throws DecisionException, ThreadStackEmptyException;
}