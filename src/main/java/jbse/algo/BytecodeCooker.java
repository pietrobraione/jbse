package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

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
     * @throws InvalidTypeException possibly raised if some 
     *         type conversion is invalid (should never happen).
     * @throws InvalidOperatorException possibly raised if the 
     *         wrong operator is used while calculating a result 
     *         (should never happen).
     * @throws InvalidOperandException possibly raised if {@code null}
     *         is pushed on the stack of a calculator (should never happen).
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
     * @throws RenameUnsupportedException possibly raised if the wrong
     *         model class is selected (should never happen).
     */
    void cook(State state) 
    throws DecisionException, ClasspathException, InvalidInputException, 
    InvalidTypeException, InvalidOperatorException, InvalidOperandException, 
    ThreadStackEmptyException, CannotManageStateException, InterruptException, 
    ContradictionException, RenameUnsupportedException;
}
