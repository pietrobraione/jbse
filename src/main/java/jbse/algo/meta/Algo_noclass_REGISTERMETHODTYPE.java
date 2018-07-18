package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * An {@link Algorithm} for an auxiliary method for the implementation of
 * {@link Algo_JAVA_METHODHANDLENATIVES_RESOLVE}. It registers in the current
 * state the mapping between a descriptor and a {@link ReferenceConcrete} to a
 * {@code java.lang.invoke.MethodType}. Its first parameter is a {@code java.lang.String}
 * and it is the descriptor, its second parameter is the {@code java.lang.invoke.MethodType}
 * produced by a call to {@code java.lang.invoke.MethodHandleNatives.findMethodHandleType}.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_noclass_REGISTERMETHODTYPE extends Algo_INVOKEMETA_Nonbranching {
    private String descriptor; //set by cookMore
    private ReferenceConcrete methodType; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws FrozenStateException {
        try {
            this.descriptor = valueString(state, (Reference) this.data.operand(0));
            if (this.descriptor == null) {
                //this should never happen
                failExecution("Unexpected null value while registering a MethodType.");
            }
            this.methodType = (ReferenceConcrete) this.data.operand(1);
        } catch (ClassCastException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.setReferenceToInstance_JAVA_METHODTYPE(this.descriptor, this.methodType);
        };
    }
}
