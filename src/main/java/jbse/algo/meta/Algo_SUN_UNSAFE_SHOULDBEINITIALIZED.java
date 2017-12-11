package jbse.algo.meta;

import static jbse.algo.Util.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
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
    protected void cookMore(State state) throws SymbolicValueNotAllowedException {
        try {
            final Reference refParam = (Reference) this.data.operand(1);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refParam);
            final String className = clazz.representedClass();
            final Klass k = state.getKlass(className);
            this.shouldBeInitialized = (k == null || !k.isInitialized());
        } catch (ClassCastException | NullPointerException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(state.getCalculator().valInt(this.shouldBeInitialized ? 1 : 0));
    }
}
