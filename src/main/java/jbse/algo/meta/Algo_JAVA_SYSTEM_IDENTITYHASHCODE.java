package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.mem.Objekt;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.System#identityHashCode(Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_SYSTEM_IDENTITYHASHCODE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final Reference thisReference = (Reference) this.data.operand(0);
                final Objekt thisObjekt = state.getObject(thisReference);

                //gets the hash code stored in the objekt and returns it
                final Primitive hashCode = thisObjekt.getIdentityHashCode();
                state.pushOperand(hashCode);
            } catch (ClassCastException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }
}
