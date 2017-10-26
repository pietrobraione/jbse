package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.common.Type.INT;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.exc.InvalidTypeException;

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
        try {
            state.pushOperand(state.getCalculator().valAny().widen(INT));
        } catch (InvalidTypeException e) {
            //this should never happen
            failExecution(e);
        }
    }
}
