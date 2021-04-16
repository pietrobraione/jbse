package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.continueWithBaseLevelImpl;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.common.exc.ClasspathException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

/**
 * Meta-level implementation of {@link java.lang.String#hashCode()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_STRING_HASHCODE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    private Primitive hash; //set by cookMore

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, InterruptException, 
    ClasspathException, FrozenStateException {
        try {
            final Reference thisReference = (Reference) this.data.operand(0);
            final Objekt thisObject = state.getObject(thisReference);
            final Reference arrayOfCharsReference = (Reference) thisObject.getFieldValue(JAVA_STRING_VALUE);
            final boolean isSimple = ((arrayOfCharsReference instanceof ReferenceConcrete) || state.resolved((ReferenceSymbolic) arrayOfCharsReference)) && 
                                     ((Array) state.getObject(arrayOfCharsReference)).isSimple();
            if (isSimple) {
                //executes the String.hashCode implementation
                continueWithBaseLevelImpl(state, this.isInterface, this.isSpecial, this.isStatic); 
            } else {
                //here the only sensible thing that we can do is to return the identity hash code
                this.hash = thisObject.getIdentityHashCode();
                //TODO possibly refine the state to ensure hash code semantics for strings based on potential equality
                //TODO possibly refine the state to ensure hash code is different to zero (?)
                //TODO possibly store the hashcode in JAVA_STRING_HASH
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.hash);
        };
    }
}
