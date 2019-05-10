package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Abstract class for all the Strategies for executing a symbolic
 * execution step.
 * 
 * @author Pietro Braione
 */
public interface Action {
    /**
     * Executes the action.
     * 
     * @param state the {@link State} on which the action must operate.
     * @param ctx the {@link ExecutionContext}.
     * @throws DecisionException possibly raised if the action uses a 
     *         decision procedure and the decision procedure fails.
     * @throws ContradictionException possibly raised if the action execution
     *         results in no successor states, which happens whenever all the 
     *         candidate successors (and thus {@code state}) fail to satisfy 
     *         the execution assumptions. 
     * @throws ClasspathException possibly raised if some core 
     *         standard class is missing from the classpath of ill-formed.
     * @throws CannotManageStateException possibly raised if the 
     *         action cannot be executed due to limitations of JBSE.
     * @throws FailureException
     * @throws InterruptException if the execution of this action must
     *         be interrupted, and possibly followed by the execution of another
     *         action. 
     */
    public void exec(State state, ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException, ClasspathException, 
    CannotManageStateException, FailureException, 
    InterruptException;
}
