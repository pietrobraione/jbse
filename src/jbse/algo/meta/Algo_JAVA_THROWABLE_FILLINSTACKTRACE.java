package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_THROWABLE_STACKTRACE;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.mem.Instance;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;
import jbse.val.Reference;

public final class Algo_JAVA_THROWABLE_FILLINSTACKTRACE extends Algo_INVOKEMETA {
    public Algo_JAVA_THROWABLE_FILLINSTACKTRACE() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
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
        };
    }
}
