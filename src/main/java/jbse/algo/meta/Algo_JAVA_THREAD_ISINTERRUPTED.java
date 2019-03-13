package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance_JAVA_THREAD;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Thread#isInterrupted(boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_THREAD_ISINTERRUPTED extends Algo_INVOKEMETA_Nonbranching {
    private Instance_JAVA_THREAD currentThread; //set by cookMore
    private Primitive isInterrupted; //set by cookMore
    private boolean clearInterrupted; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) 
    throws SymbolicValueNotAllowedException, ClasspathException, 
    InterruptException, FrozenStateException {
        try {
            //there's only one thread in JBSE, so skips the first parameter
            //and gets the current thread from the context
            this.currentThread = (Instance_JAVA_THREAD) state.getObject(state.getMainThread()); 
            this.isInterrupted = this.ctx.getCalculator().valInt(currentThread.isInterrupted() ? 1 : 0);

            //gets the second (boolean ClearInterrupted) parameter
            final Primitive _clearInterrupted = (Primitive) this.data.operand(1);
            if (_clearInterrupted.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The boolean ClearInterrupted parameter to invocation of method java.lang.Thread.isInterrupted method cannot be a symbolic value.");
            }
            this.clearInterrupted = (((Integer) ((Simplex) _clearInterrupted).getActualValue()).intValue() > 0);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }        
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.isInterrupted);
            if (this.clearInterrupted) {
                this.currentThread.setInterrupted(false);
            }
        };
    }
}
