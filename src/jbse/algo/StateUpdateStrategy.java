package jbse.algo;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.State;

/**
 * Strategy for updating a state, i.e., for completing its 
 * bytecode semantics after a refinement.
 * 
 * @author Pietro Braione
 *
 * @param <R> the type of the possible outcome of a previous decision.
 */
@FunctionalInterface
interface StateUpdateStrategy<R> {
	/**
	 * Updates a state, i.e., completes its current bytecode's 
	 * semantics after a refinement.
	 * 
	 * @param s the {@link State} to be updated.
	 * @param r the outcome of a previous decision, which establishes 
	 *          the criterion on which the state shall be updated.
	 * @throws DecisionException
	 * @throws ThreadStackEmptyException
	 * @throws UnexpectedInternalException 
	 */
	public void update(State s, R r) 
	throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException;
}