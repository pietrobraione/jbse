package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.Algorithm;
import jbse.mem.State;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which disables violation of assumptions.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_ANALYSIS_DISABLEASSUMPTIONVIOLATION extends Algo_INVOKEMETA {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }
    
    @Override
    protected void update(State state) {
        state.disableAssumptionViolation();
    }
}
