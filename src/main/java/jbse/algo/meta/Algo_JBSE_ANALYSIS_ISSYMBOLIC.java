package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

/**
 * An {@link Algorithm} implementing {@link jbse.meta.Analysis#isSymbolic(boolean)},
 * {@link jbse.meta.Analysis#isSymbolic(byte)},
 * {@link jbse.meta.Analysis#isSymbolic(char)},
 * {@link jbse.meta.Analysis#isSymbolic(double)},
 * {@link jbse.meta.Analysis#isSymbolic(float)},
 * {@link jbse.meta.Analysis#isSymbolic(int)},
 * {@link jbse.meta.Analysis#isSymbolic(long)}, and
 * {@link jbse.meta.Analysis#isSymbolic(short)}.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_ANALYSIS_ISSYMBOLIC extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        final Value arg = this.data.operand(0);
        state.pushOperand(state.getCalculator().valInt((arg.isSymbolic() ? 1 : 0)));
    }
}
