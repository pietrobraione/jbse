package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#shouldBeInitialized(Class)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_SHOULDBEINITIALIZED extends Algo_INVOKEMETA_Nonbranching {
    private boolean shouldBeInitialized; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) 
    throws SymbolicValueNotAllowedException, FrozenStateException {
        try {
            final Reference refParam = (Reference) this.data.operand(1);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refParam);
            final ClassFile classFile = clazz.representedClass();
            final Klass k = state.getKlass(classFile);
            this.shouldBeInitialized = (k == null || !k.initializationCompleted());
        } catch (ClassCastException | NullPointerException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.ctx.getCalculator().valInt(this.shouldBeInitialized ? 1 : 0));
        };
    }
}
