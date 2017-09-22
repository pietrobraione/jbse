package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.common.Type.className;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.toPrimitiveBinaryClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;
import jbse.val.Reference;

public final class Algo_JAVA_CLASS_GETCOMPONENTTYPE extends Algo_INVOKEMETA_Nonbranching {
    private Reference componentClassRef;  //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {           
            //gets the binary name of the primitive type and converts it to a string
            final Reference classRef = (Reference) this.data.operand(0);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            final String className = clazz.representedClass();
            if (isArray(className)) {
                final String componentTypeName = getArrayMemberType(className);
                if (isPrimitive(componentTypeName)) {
                    final String componentTypeNameBinary = toPrimitiveBinaryClassName(componentTypeName);
                    state.ensureInstance_JAVA_CLASS_primitive(componentTypeNameBinary);
                    this.componentClassRef = state.referenceToInstance_JAVA_CLASS_primitive(componentTypeNameBinary);
                } else {
                    final String componentClassName = className(componentTypeName);
                    ensureInstance_JAVA_CLASS(state, componentClassName, componentClassName, this.ctx);
                    this.componentClassRef = state.referenceToInstance_JAVA_CLASS(componentClassName);
                }
            } else {
                //not an array
                this.componentClassRef = Null.getInstance();
            }
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            throwNew(state, CLASS_NOT_FOUND_EXCEPTION);  //this is how Hotspot behaves
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) 
    throws SymbolicValueNotAllowedException, ThreadStackEmptyException, InterruptException {
        state.pushOperand(this.componentClassRef);
    }
}
