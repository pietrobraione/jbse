package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;

/**
 * Meta-level implementation of {@link java.lang.Class#getProtectionDomain0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETPROTECTIONDOMAIN0 extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(Null.getInstance()); //sorry, JBSE has no protection domains
        };
    }
}
