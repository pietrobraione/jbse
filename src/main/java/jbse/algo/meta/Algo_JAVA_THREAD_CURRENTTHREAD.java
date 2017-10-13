package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Meta-level implementation of {@link java.lang.Thread#currentThread()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_THREAD_CURRENTTHREAD extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void update(State state) 
    throws ThreadStackEmptyException, InterruptException {
        try {
            state.pushOperand(this.ctx.getMainThread()); //there's only one thread in JBSE!
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
