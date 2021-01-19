package jbse.algo;

import java.util.SortedSet;

import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;

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
     * @throws ClasspathException possibly raised if some core 
     *         standard class is missing from the classpath of ill-formed.
     * @throws InterruptException possibly raised if the execution 
     *         of the bytecode semantics must be interrupted, e.g., 
     *         if some correctness check fails or if heap memory
     *         is exhausted.
     * @throws InterruptException possibly raised if the execution 
     *         of the bytecode semantics must be interrupted, e.g., 
     *         if some correctness check fails or if heap memory
     *         is exhausted.
     * @throws ContradictionException possibly raised by guidance
     *         decision procedure if some class initialization assumption
     *         during backdoor expansion is violated
     */
    public Outcome decide(State state, SortedSet<R> result) 
    throws InvalidInputException, DecisionException,
    ClasspathException, InterruptException, ContradictionException;
}