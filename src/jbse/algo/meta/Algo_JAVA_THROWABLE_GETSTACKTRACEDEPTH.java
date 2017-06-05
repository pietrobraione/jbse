package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference thisRef = (Reference) this.data.operand(0);
            final Array backtrace = (Array) state.getObject((Reference) state.getObject(thisRef).getFieldValue(JAVA_THROWABLE_BACKTRACE));
            state.pushOperand(backtrace.getLength());
        } catch (ClassCastException | NullPointerException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
