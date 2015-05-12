package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.tree.DecisionAlternative_NONE;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which pushes the any value on the stack.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_ANALYSIS_ANY extends Algo_INVOKEMETA {
    public Algo_JBSE_ANALYSIS_ANY() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(state.getCalculator().valAny());
        };
    }
}
