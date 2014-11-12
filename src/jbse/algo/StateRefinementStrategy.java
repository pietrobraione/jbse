package jbse.algo;

import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.State;

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