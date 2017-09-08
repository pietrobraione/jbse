package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;

public final class Algo_JAVA_CLASS_GETCLASSLOADER0 extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void update(State state) 
    throws SymbolicValueNotAllowedException, ThreadStackEmptyException, InterruptException {
        state.pushOperand(Null.getInstance()); //as everything were loaded by the bootstrap class loader
        //TODO implement classloading and fix this method
    }
}
