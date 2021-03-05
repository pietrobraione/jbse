package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT extends Algo_INVOKEMETA_Nonbranching {
    private Primitive index; //set by cookMore
    private Array backtrace; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {
            final Reference thisObject = (Reference) this.data.operand(0);
            this.index = (Primitive) this.data.operand(1);
            if (!(this.index instanceof Simplex)) { //quite unlikely...
                throw new SymbolicValueNotAllowedException("the index parameter to java.lang.Throwable.getStackTraceElement method cannot be a symbolic int");
            }
            final int indexInt = (int) ((Simplex) this.index).getActualValue();
            this.backtrace = (Array) state.getObject((Reference) state.getObject(thisObject).getFieldValue(JAVA_THROWABLE_BACKTRACE));
            final int stackDepth = (int) ((Simplex) this.backtrace.getLength()).getActualValue();
            if (indexInt < 0 || indexInt >= stackDepth) {
                throwNew(state, this.ctx.getCalculator(), INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final Value toPush = ((AccessOutcomeInValue) this.backtrace.get(this.ctx.getCalculator(), this.index).iterator().next()).getValue();
                state.pushOperand(toPush);
            } catch (InvalidTypeException | ThreadStackEmptyException | ClassCastException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
}
