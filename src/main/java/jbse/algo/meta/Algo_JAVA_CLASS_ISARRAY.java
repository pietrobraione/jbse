package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#isArray()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_ISARRAY extends Algo_INVOKEMETA_Nonbranching {
    private Simplex isArray; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, InterruptException, ClasspathException, FrozenStateException {
        try {
            //gets the 'this' java.lang.Class instance from the heap 
            //and the name of the class it represents
            final Reference javaClassRef = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isArray method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (clazz == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.isArray method is symbolic and unresolved.");
            }
            this.isArray = this.ctx.getCalculator().valInt(clazz.representedClass().isArray() ? 1 : 0);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.isArray);
        };
    }
}
