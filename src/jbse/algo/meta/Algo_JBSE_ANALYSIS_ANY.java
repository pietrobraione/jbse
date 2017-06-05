package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which pushes the any value on the stack.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_ANALYSIS_ANY extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(state.getCalculator().valAny());
    }
}
