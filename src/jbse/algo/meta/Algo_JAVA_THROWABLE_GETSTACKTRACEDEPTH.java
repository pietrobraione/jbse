package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH extends Algo_INVOKEMETA {
    public Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //TODO replace this dummy implementation
            state.pushOperand(state.getCalculator().valInt(0));
        };
    }
}
