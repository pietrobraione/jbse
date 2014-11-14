package jbse.algo;

import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.val.exc.InvalidTypeException;

/**
 * Strategy for refining a {@link State}.
 * 
 * @author Pietro Braione
 *
 * @param <R> the type of the possible outcomes of a previous decision.
 */
@FunctionalInterface
interface StateRefinementStrategy<R> {
	/**
	 * Refines a {@link State}. The implementors must augment the 
	 * state's {@link PathCondition}, and possibly refine references 
	 * and symbolic objects.
	 * 
	 * @param s the {@link State} to be refined.
	 * @param r the outcome of a previous decision, which establishes 
	 *          the criterion on which the state shall be refined.
	 * @throws DecisionException
	 * @throws ContradictionException 
	 * @throws InvalidTypeException
	 */
	public void refine(State s, R r) 
	throws DecisionException, ContradictionException, InvalidTypeException;
}