package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

public final class Algo_JBSE_ANALYSIS_SUCCEED extends Algo_INVOKEMETA {
    public Algo_JBSE_ANALYSIS_SUCCEED() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) ->{
            state.setStuckStop();
        };
    }
}
