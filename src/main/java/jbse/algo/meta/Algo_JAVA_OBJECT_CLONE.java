package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.CLONE_NOT_SUPPORTED_EXCEPTION;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.ClasspathException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Object#clone()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_OBJECT_CLONE extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile classFile; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, FrozenStateException {
        try {
            //gets the "this" object and the name of its class
            final Reference thisRef = (Reference) this.data.operand(0);
            if (state.isNull(thisRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Object.clone method is null.");
            }
            final Objekt thisObj = state.getObject(thisRef);
            if (thisObj == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Object.clone method is symbolic and unresolved.");
            }
            this.classFile = thisObj.getType();
            boolean isCloneable = false;
            for (ClassFile superinterface : this.classFile.superinterfaces()) {
            	if (JAVA_CLONEABLE.equals(superinterface.getClassName())) {
            		isCloneable = true;
            		break;
            	}
            }
            if (!isCloneable) {
                throwNew(state, this.ctx.getCalculator(), CLONE_NOT_SUPPORTED_EXCEPTION);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            try {
                final Reference thisRef = (Reference) this.data.operand(0);
                final Objekt thisObj = state.getObject(thisRef);
                final Reference cloneRef;
                if (this.classFile.isArray()) {
                    //creates the clone
                    cloneRef = state.createArray(calc, null, ((Array) thisObj).getLength(), this.classFile);
                    final Array cloneObj = (Array) state.getObject(cloneRef);

                    //populates the clone
                    cloneObj.cloneEntries((Array) thisObj, calc);
                } else {
                    //creates the clone
                    cloneRef = state.createInstance(calc, this.classFile);
                    final Instance cloneObj = (Instance) state.getObject(cloneRef);

                    //populates the clone
                    for (Signature sigField : thisObj.getStoredFieldSignatures()) {
                        final Value thisObjFieldValue = thisObj.getFieldValue(sigField);
                        cloneObj.setFieldValue(sigField, thisObjFieldValue);
                    }
                }
                
                //pushes the reference to the clone
                state.pushOperand(cloneRef);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (InvalidTypeException | ClassCastException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
}
