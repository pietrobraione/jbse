package jbse.algo.meta;

import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.mem.State;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT extends Algo_INVOKEMETA {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void update(State state) {
        //TODO replace this dummy implementation
        throwNew(state, INDEX_OUT_OF_BOUNDS_EXCEPTION);
    }
}
