package jbse.algo.meta;

import static jbse.algo.Util.continueWith;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_NONBRANCHING;
import jbse.algo.InterruptException;
import jbse.mem.State;

public final class Algo_JAVA_REFLECT_ARRAY_NEWARRAY extends Algo_INVOKEMETA_NONBRANCHING {
    private final Algo_JAVA_REFLECT_ARRAY_NEWARRAY_COMPLETION algo = new Algo_JAVA_REFLECT_ARRAY_NEWARRAY_COMPLETION();
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws InterruptException {
        continueWith(this.algo);
    }

    @Override
    protected void update(State state)  {
        //nothing to do    
    }
}
