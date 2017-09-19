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
public interface StrategyRefine<R> {
	/**
	 * Refines a {@link State}. The implementors must augment the 
	 * state's {@link PathCondition}, and possibly do other actions 
	 * on the structure of the state to reflect the additional
	 * assumption.
	 * 
	 * @param s the {@link State} to be refined.
	 * @param r the outcome of a previous decision, which establishes 
	 *          the criterion on which the state shall be refined.
	 * @throws DecisionException possibly raised when the refinement 
	 *         action must simplify some symbolic array entries.
	 * @throws ContradictionException possibly raised when the refinement
	 *         action leads to a contradiction in the path condition.
	 * @throws InvalidTypeException possibly raised when using a value 
	 *         with the wrong type in the refinement action.
	 */
	public void refine(State s, R r) 
	throws DecisionException, ContradictionException, InvalidTypeException;
}