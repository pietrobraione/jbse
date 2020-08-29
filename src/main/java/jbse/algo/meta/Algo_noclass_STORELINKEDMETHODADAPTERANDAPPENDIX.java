package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODTYPE_METHODDESCRIPTOR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.Signature;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * An {@link Algorithm} for completing the semantics of
 * linking of a signature polymorphic nonintrinsic method 
 * (usually from an invokehandle bytecode).
 * The first parameter is a {@link ReferenceConcrete} to a {@code String} with 
 * the name of the method (either {@code invoke} or {@code invokeExact}), 
 * the second parameter is a {@link ReferenceConcrete} to a 
 * {@code java.lang.invoke.MethodType} for the resolved method, 
 * the third parameter is a {@link ReferenceConcrete} to the appendix, 
 * the fourth parameter is a {@link ReferenceConcrete} to the {@code java.lang.invoke.MemberName}
 * returned by the upcall to {@code java.lang.invoke.MethodHandleNatives.linkMethod}.
 * 
 * @author Pietro Braione
 */
public final class Algo_noclass_STORELINKEDMETHODADAPTERANDAPPENDIX extends Algo_INVOKEMETA_Nonbranching {
    private Signature methodSignature; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        final String methodName = valueString(state, (ReferenceConcrete) this.data.operand(0));
        final Instance methodType = (Instance) state.getObject((ReferenceConcrete) this.data.operand(1));
        final String methodDescriptor = valueString(state, (Reference) methodType.getFieldValue(JAVA_METHODTYPE_METHODDESCRIPTOR));
        if (methodName == null || methodDescriptor == null) {
            //this should never happen
            failExecution("Unexpected null value while trying to store in the state the linked invoker/appendix for a signature polymorphic method.");
        }
        this.methodSignature = new Signature(JAVA_METHODHANDLE, methodDescriptor, methodName); //note that the invoke and invokeExact method are both in java.lang.invoke.MethodHandle
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.linkMethod(this.methodSignature, (ReferenceConcrete) this.data.operand(3), (ReferenceConcrete) this.data.operand(2));
        };
    }
}
