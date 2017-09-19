package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;

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
     *         standard class is missing from the classpath.
     * @throws CannotManageStateException possibly raised if the 
     *         bytecode cannot be executed due to limitations of JBSE.
     * @throws InterruptException possibly raised if the execution 
     *         of the bytecode semantics must be interrupted, e.g., 
     *         if some correctness check fails.
     */
    void cook(State state) 
    throws DecisionException, ClasspathException, 
    CannotManageStateException, InterruptException;
}
