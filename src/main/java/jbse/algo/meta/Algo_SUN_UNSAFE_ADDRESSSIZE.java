package jbse.algo.meta;

import static jbse.common.Util.unsafe;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#addressSize()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_ADDRESSSIZE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final int addressSize = unsafe().addressSize();
            state.pushOperand(this.ctx.getCalculator().valInt(addressSize));
        };
    }
}
