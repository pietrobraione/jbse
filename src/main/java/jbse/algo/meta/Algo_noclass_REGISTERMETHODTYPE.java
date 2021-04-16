package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.mem.Instance_METALEVELBOX;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * An {@link Algorithm} for an auxiliary method for the implementation of
 * method type creation. It registers in the current
 * state the mapping between a descriptor and a {@link ReferenceConcrete} to a
 * {@code java.lang.invoke.MethodType}. Its first parameter is the 
 * {@code java.lang.invoke.MethodType} produced by a call to 
 * {@code java.lang.invoke.MethodHandleNatives.findMethodHandleType}, its second 
 * parameter is a meta-level box containing an array of {@link ClassFile}s, the 
 * resolved descriptor.
 * 
 * @author Pietro Braione
 */
public final class Algo_noclass_REGISTERMETHODTYPE extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete methodType; //set by cookMore
    private ClassFile[] descriptor; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws FrozenStateException {
        try {
            this.methodType = (ReferenceConcrete) this.data.operand(0);
        	final Reference refDescriptor = (Reference) this.data.operand(1);
        	final Instance_METALEVELBOX b = (Instance_METALEVELBOX) state.getObject(refDescriptor);
            this.descriptor = (ClassFile[]) b.get();
            if (this.descriptor == null) {
                //this should never happen
                failExecution("Unexpected null value in meta-level box while registering a MethodType.");
            }
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
