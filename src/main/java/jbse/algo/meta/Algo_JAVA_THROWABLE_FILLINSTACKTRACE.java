package jbse.algo.meta;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.fillExceptionBacktrace;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_THROWABLE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Throwable#fillInStackTrace(int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_THROWABLE_FILLINSTACKTRACE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException, ContradictionException, RenameUnsupportedException {
        try {
            final ClassFile cf_JAVA_STRING = state.getClassHierarchy().loadCreateClass(JAVA_STRING);
            ensureClassInitialized(state, this.ctx, cf_JAVA_STRING);
            final ClassFile cf_JAVA_THROWABLE = state.getClassHierarchy().loadCreateClass(JAVA_THROWABLE);
            ensureClassInitialized(state, this.ctx, cf_JAVA_THROWABLE);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                 WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final Reference excReference = (Reference) this.data.operand(0);
                fillExceptionBacktrace(state, this.ctx.getCalculator(), excReference);
                state.pushOperand(excReference); //returns "this"
            } catch (ClassCastException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }
}
