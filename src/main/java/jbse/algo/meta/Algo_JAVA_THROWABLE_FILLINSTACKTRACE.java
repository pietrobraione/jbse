package jbse.algo.meta;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.fillExceptionBacktrace;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_THROWABLE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Throwable#fillInStackTrace(int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_THROWABLE_FILLINSTACKTRACE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {
            ensureClassCreatedAndInitialized(state, JAVA_STRING, this.ctx);
            ensureClassCreatedAndInitialized(state, JAVA_THROWABLE, this.ctx);
        } catch (BadClassFileException e) {
            throw new ClasspathException(e);
        } catch (InvalidInputException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference excReference = (Reference) this.data.operand(0);
            fillExceptionBacktrace(state, excReference);
            state.pushOperand(excReference); //returns "this"
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
