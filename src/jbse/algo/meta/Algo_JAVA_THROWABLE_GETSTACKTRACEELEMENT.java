package jbse.algo.meta;

import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT extends Algo_INVOKEMETA {
    public Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //TODO replace this dummy implementation
            throwNew(state, INDEX_OUT_OF_BOUNDS_EXCEPTION);
        };
    }
}
