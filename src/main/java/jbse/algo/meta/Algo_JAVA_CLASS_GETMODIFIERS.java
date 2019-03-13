package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isPrimitive()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETMODIFIERS extends Algo_INVOKEMETA_Nonbranching {
    private Simplex modifiers; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject((Reference) this.data.operand(0));
            if (clazz == null) {
                //this should never happen
                failExecution("violated invariant (unexpected heap access with symbolic unresolved reference)");
            }
            final ClassFile cf = clazz.representedClass();
            this.modifiers = this.ctx.getCalculator().valInt(cf.getModifiers());
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.modifiers);
        };
    }
}
