package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_THROWABLE_STACKTRACE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.InterruptException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;
import jbse.val.Reference;

public final class Algo_JAVA_THROWABLE_FILLINSTACKTRACE extends Algo_INVOKEMETA {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference thisRef = (Reference) this.data.operand(0);
            final Instance exc = (Instance) state.getObject(thisRef);

            //TODO replace this dummy implementation
            exc.setFieldValue(JAVA_THROWABLE_STACKTRACE, Null.getInstance());

            state.pushOperand(thisRef); //returns "this"
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
