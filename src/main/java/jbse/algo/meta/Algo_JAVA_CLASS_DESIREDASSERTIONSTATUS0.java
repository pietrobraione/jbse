package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

public final class Algo_JAVA_CLASS_DESIREDASSERTIONSTATUS0 extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(state.getCalculator().valBoolean(false)); //no assertions, sorry
        //TODO should we give a way to control the assertion status, possibly handling Java assertions as jbse assertions?
    }
}
