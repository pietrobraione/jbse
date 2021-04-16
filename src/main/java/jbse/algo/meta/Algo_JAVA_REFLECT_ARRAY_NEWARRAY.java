package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.continueWith;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Meta-level implementation of {@link java.lang.reflect.Array#newArray(Class, int)}.
 * This {@link Algorithm} starts the execution.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_REFLECT_ARRAY_NEWARRAY extends Algo_INVOKEMETA_Nonbranching {
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
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //nothing to do
        };
    }
}
