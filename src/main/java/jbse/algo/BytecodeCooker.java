package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Strategy that extracts or calculates additional 
 * data necessary to perform the bytecode semantics, 
 * perform resolution of symbolic references if it 
 * is the case, and check the resulting data for 
 * correctness and/or permissions, possibly interrupting
 * the execution of the bytecode.
 * 
 * @author Pietro Braione
 */
@FunctionalInterface
public interface BytecodeCooker {
    /**
     * Extracts or calculates additional data 
     * necessary to perform the bytecode semantics, 
     * perform resolution of symbolic references if it 
     * is the case, and check the resulting data for 
     * correctness and/or permissions, possibly interrupting
     * the execution of the bytecode. 
     * 
     * @param state the {@link State} from which the data
     *        should be extracted and that should be checked.
     * @throws DecisionException possibly raised when performing
     *         class creation and initialization.
     * @throws ClasspathException possibly raised if some core 
     *         standard class is missing from the classpath of ill-formed.
     * @throws InvalidInputException possibly raised if some 
     *         input to some method call is invalid (should never happen).
     * @throws ThreadStackEmptyException possibly raised if the
     *         thread stack is empty (should never happen).
     * @throws CannotManageStateException possibly raised if the 
     *         bytecode cannot be executed due to limitations of JBSE.
     * @throws InterruptException possibly raised if the execution 
     *         of the bytecode semantics must be interrupted, e.g., 
     *         if some correctness check fails or if heap memory
     *         is exhausted.
     * @throws ContradictionException possibly raised if some initialization
     *         assumption is contradicted (currently JBSE does not manage
     *         initialization in the decider).
     */
    void cook(State state) 
    throws DecisionException, ClasspathException, InvalidInputException, 
    ThreadStackEmptyException, CannotManageStateException, InterruptException, 
    ContradictionException;
}
