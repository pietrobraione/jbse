package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

public final class Algo_SUN_REFLECTION_GETCLASSACCESSFLAGS extends Algo_INVOKEMETA_Nonbranching {
    private int flags; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) throws FrozenStateException {
        try {
            final Reference refParam = (Reference) this.data.operand(0);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refParam);
            final ClassFile cf = clazz.representedClass();
            this.flags = cf.getAccessFlags();
        } catch (ClassCastException | NullPointerException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.ctx.getCalculator().valInt(this.flags));
        };
    }
}
