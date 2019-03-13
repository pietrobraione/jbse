package jbse.algo;

import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
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
     * @throws InvalidOperatorException possibly raised if the 
     *         wrong operator is used while calculating a result 
     *         (should never happen).
     * @throws InvalidOperandException possibly raised if {@code null}
     *         is pushed on the stack of a calculator (should never happen).
     * @throws InterruptException  possibly raised if the execution 
     *         of the bytecode semantics must be interrupted, in this
     *         case only because of heap memory exhaustion.
     * @throws SymbolicValueNotAllowedException possibly raised when the 
     *         refinement action leads to assuming a symbolic object in 
     *         the initial state that is disallowed (currently 
     *         {@code java.lang.Class} or {@code java.lang.ClassLoader}
     *         objects).
     * @throws ClasspathException possibly raised when the refinement
     *         action must throw a standard exception and the exception 
     *         is not found in the classpath.
     * @throws InvalidInputException possibly raised when an input is invalid
     *         (currently, only if {@code s} is frozen) or unexpectedly if
     *         assuming a wrong {@link Primitive} during a numeric assumption
     *         (either {@code null}, or not boolean, or neither {@link Simplex}
     *         nor {@link Expression}).
     */
    public void refine(State s, R r) 
    throws DecisionException, ContradictionException, InvalidTypeException, InvalidOperatorException, 
    InvalidOperandException, InterruptException, SymbolicValueNotAllowedException, ClasspathException, 
    InvalidInputException;
}