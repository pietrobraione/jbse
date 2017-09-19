package jbse.algo;

import java.util.SortedSet;

import jbse.bc.exc.BadClassFileException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;

/**
 * Strategy for deciding the states following the current one.
 * 
 * @author Pietro Braione
 * 
 * @param <R> the type of the possible outcomes of the decision.
 */
@FunctionalInterface
public interface StrategyDecide<R> {
	/**
	 * Decides the states following the current one.
	 * 
	 * @param state the {@link State} whose successors must be decided.
	 * @param result a (possibly empty) {@link SortedSet}{@code <R>}, 
	 *        which the method will update by storing in it an 
	 *        instance of {@code R} for each possible outcome of 
	 *        the decision. 
	 * @return an {@link Outcome}.
	 * @throws InvalidInputException possibly thrown by the decision procedure
	 *         when invoked with invalid parameters.
     * @throws DecisionException possibly thrown by the decision procedure
     *         upon failure.
	 * @throws BadClassFileException possibly thrown by the decision procedure
     *         when performing reference resolution, if some classfile is
     *         missing or is incompatible with the current JBSE.
	 */
	public Outcome decide(State state, SortedSet<R> result) 
	throws InvalidInputException, BadClassFileException, DecisionException;
}