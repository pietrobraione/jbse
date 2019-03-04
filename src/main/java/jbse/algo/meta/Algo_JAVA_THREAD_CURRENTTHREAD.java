package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

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
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                state.pushOperand(state.getMainThread()); //there's only one thread in JBSE!
            } catch (ClassCastException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }
}
