package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_NONBRANCHING;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH extends Algo_INVOKEMETA_NONBRANCHING {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //TODO replace this dummy implementation
        state.pushOperand(state.getCalculator().valInt(0));
    }
}
