package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.CLONE_NOT_SUPPORTED_EXCEPTION;
import static jbse.common.Type.isArray;

import java.util.List;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Object#clone()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_OBJECT_CLONE extends Algo_INVOKEMETA_Nonbranching {
    private String className; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, 
    ClasspathException, SymbolicValueNotAllowedException, 
    InterruptException {
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
            this.className = thisObj.getType();
            final List<String> superinterfaces = state.getClassHierarchy().getClassFile(this.className).getSuperInterfaceNames();
            if (!superinterfaces.contains(JAVA_CLONEABLE)) {
                throwNew(state, CLONE_NOT_SUPPORTED_EXCEPTION);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        try {
            final Reference thisRef = (Reference) this.data.operand(0);
            final Objekt thisObj = state.getObject(thisRef);
            final Reference cloneRef;
            if (isArray(this.className)) {
                //creates the clone
                cloneRef = state.createArray(null, ((Array) thisObj).getLength(), this.className);
                final Array cloneObj = (Array) state.getObject(cloneRef);

                //populates the clone
                cloneObj.cloneEntries((Array) thisObj);
            } else {
                //creates the clone
                cloneRef = state.createInstance(this.className);
                final Instance cloneObj = (Instance) state.getObject(cloneRef);

                //populates the clone
                for (Signature sigField : thisObj.getStoredFieldSignatures()) {
                    final Value thisObjFieldValue = thisObj.getFieldValue(sigField);
                    cloneObj.setFieldValue(sigField, thisObjFieldValue);
                }
            }
            
            //pushes the reference to the clone
            state.pushOperand(cloneRef);
        } catch (InvalidTypeException | ClassCastException e) {
            //this should never happen
            failExecution(e);
        }
    }
}
