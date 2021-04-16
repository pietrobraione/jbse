package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Instance_METALEVELBOX;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;

/**
 * An {@link Algorithm} for an auxiliary method for the implementation of
 * method handle creation. It registers in the current
 * state the mapping between a key and a {@link ReferenceConcrete} to a
 * {@code java.lang.invoke.MethodHandle}. Its first parameter is the 
 * {@code java.lang.invoke.MethodHandle} produced by a call to 
 * {@code java.lang.invoke.MethodHandleNatives.linkMethodHandleConstant}, its second 
 * to fifth parameters are the key. They are respectively: the int representing the 
 * method handle behavior, a reference to an {@link Instance_JAVA_CLASS} representing 
 * the container class of the field/method, a meta-level box containing an array of 
 * {@link ClassFile}s, the resolved descriptor, and finally a reference to an 
 * {@link Instance} of class {@link String} containing the name of the field/method. 
 * 
 * @author Pietro Braione
 */
public final class Algo_noclass_REGISTERMETHODHANDLE extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete methodHandle; //set by cookMore
    private int refKind; //set by cookMore
    private ClassFile container; //set by cookMore
    private ClassFile[] descriptor; //set by cookMore
    private String name; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }
    
    @Override
    protected void cookMore(State state) throws FrozenStateException {
        try {
            this.methodHandle = (ReferenceConcrete) this.data.operand(0);
            this.refKind = ((Integer) ((Simplex) this.data.operand(1)).getActualValue()).intValue();
            final Reference refContainer = (Reference) this.data.operand(2);
            final Instance_JAVA_CLASS container = (Instance_JAVA_CLASS) state.getObject(refContainer);
            this.container = container.representedClass();
        	final Reference refDescriptor = (Reference) this.data.operand(3);
        	final Instance_METALEVELBOX b = (Instance_METALEVELBOX) state.getObject(refDescriptor);
            this.descriptor = (ClassFile[]) b.get();
            if (this.descriptor == null) {
                //this should never happen
                failExecution("Unexpected null value in meta-level box while registering a MethodHandle.");
            }
            this.name = valueString(state, (Reference) this.data.operand(4));
        } catch (ClassCastException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.setReferenceToInstance_JAVA_METHODHANDLE(this.refKind, this.container, this.descriptor, this.name, this.methodHandle);
        };
    }
}
