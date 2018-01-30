package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

/**
 * An {@link Algorithm} for an auxiliary method for the implementation of
 * {@link Algo_INIT}. It sets the current state to the post-initialization phase.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_noclass_SETPHASEPOSTINIT extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.setPhasePostInit();
        };
    }
}
