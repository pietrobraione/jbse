package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.mem.State;

public final class Algo_JBSE_ANALYSIS_SUCCEED extends Algo_INVOKEMETA {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }
    
    @Override
    protected void update(State state) {
        state.setStuckStop();
    }
}
