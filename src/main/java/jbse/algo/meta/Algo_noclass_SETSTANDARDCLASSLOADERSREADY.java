package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

/**
 * An {@link Algorithm} for an auxiliary method for the implementation of
 * {@link Action_INIT}. It sets the current state by asserting that the standard
 * (extensions and application) classloaders are ready to be used.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_noclass_SETSTANDARDCLASSLOADERSREADY extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.setStandardClassLoadersReady();
        };
    }
}
