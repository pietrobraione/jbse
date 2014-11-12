package jbse.algo;

import java.util.SortedSet;

import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.dec.DecisionException;
import jbse.exc.dec.InvalidInputException;

/**
 * Strategy for deciding the states following the current one.
 * 
 * @author Pietro Braione
 * 
 * @param <R> the type of the possible outcomes of the decision.
 */
@FunctionalInterface
interface DecisionStrategy<R> {
	/**
	 * Decides the states following the current one.
	 * 
	 * @param results a (possibly empty) {@link SortedSet}{@code <R>}, 
	 *        which the method will update by storing in it an 
	 *        instance of {@code R} for each possible outcome of 
	 *        the decision. 
	 * @return {@code true} iff some decision outcomes 
	 *         require refinement of the produced state. Note
	 *         that {@code result.size() > 1} is typically sufficient
	 *         for the return value to be {@code true}, but it may not
	 *         (e.g., for any values no refinement is necessary), and
	 *         it may not even be necessary.
	 * @throws InvalidInputException
	 * @throws ClassFileNotFoundException
	 * @throws DecisionException
	 */
	public Outcome decide(SortedSet<R> results) 
	throws InvalidInputException, ClassFileNotFoundException, DecisionException;
}