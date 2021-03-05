package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.common.exc.ClasspathException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Object#hashCode()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_OBJECT_HASHCODE extends Algo_INVOKEMETA_Nonbranching {
    private Primitive hashCode; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, 
    ClasspathException, FrozenStateException {
        try {
            final Reference thisReference = (Reference) this.data.operand(0);
            final Objekt thisObjekt = state.getObject(thisReference);

            //gets the hash code stored in the objekt and returns it
            this.hashCode = thisObjekt.getIdentityHashCode();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.hashCode);
        };
    }
}
