package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.lookupClassfileMethodImpl;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.splitParametersDescriptors;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Abstract algorithm for the invoke* bytecodes
 * (invoke[interface/special/static/virtual]).
 * 
 * @author Pietro Braione
 *
 */
abstract class Algo_INVOKEX_Abstract extends Algorithm<
BytecodeData_1ZME,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    protected final boolean isInterface; //set by the constructor
    protected final boolean isSpecial; //set by the constructor
    protected final boolean isStatic; //set by the constructor

    public Algo_INVOKEX_Abstract(boolean isInterface, boolean isSpecial, boolean isStatic) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
    }

    protected ClassFile classFileMethodImpl; //set by cooking methods
    protected boolean isNative; //set by cooking methods
    protected Signature methodSignatureResolved; //set by cooking methods
    protected Signature methodSignatureImpl; //set by cooking methods

    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> {
            final String[] paramsDescriptors = splitParametersDescriptors(this.data.signature().getDescriptor());
            return (this.isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);
        };
    }

    @Override
    protected final Supplier<BytecodeData_1ZME> bytecodeData() {
        return () -> BytecodeData_1ZME.withInterfaceMethod(this.isInterface).get();
    }

    protected final void resolveMethod(State state) 
    throws BadClassFileException, IncompatibleClassFileException, MethodAbstractException, 
    MethodNotFoundException, MethodNotAccessibleException {
        try {
            //performs method resolution
            final ClassHierarchy hier = state.getClassHierarchy();
            final String currentClassName = state.getCurrentMethodSignature().getClassName();
            this.methodSignatureResolved = hier.resolveMethod(currentClassName, this.data.signature(), this.isInterface);
        } catch (ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }
    }

    protected final void check(State state) throws InterruptException {
        //checks the resolved method; note that more checks 
        //are done later, by the last call to state.pushFrame
        try {
            final ClassHierarchy hier = state.getClassHierarchy();
            final ClassFile classFileResolved = hier.getClassFile(this.methodSignatureResolved.getClassName());
            if (classFileResolved.isMethodStatic(this.methodSignatureResolved) != this.isStatic) {
                throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
                //TODO this check is ok for invoke[interface/static/virtual], which checks should we do for invokespecial, if any?
            }
        } catch (BadClassFileException | MethodNotFoundException e) {
            //this should never happen after resolution
            failExecution(e);
        }
    }

    protected final void findImplAndCalcNative(State state) 
    throws BadClassFileException, IncompatibleClassFileException, 
    InterruptException {
        try {
            final boolean isVirtualInterface = !this.isStatic && !this.isSpecial;
            final String receiverClassName;
            if (isVirtualInterface) {
                final Reference thisRef = state.peekReceiverArg(this.methodSignatureResolved);
                if (state.isNull(thisRef)) {
                    throwNew(state, NULL_POINTER_EXCEPTION);
                    exitFromAlgorithm();
                }
                receiverClassName = state.getObject(thisRef).getType();
            } else {
                receiverClassName = null;
            }
            this.classFileMethodImpl = 
                lookupClassfileMethodImpl(state, this.methodSignatureResolved, this.isStatic, this.isSpecial, receiverClassName);
            this.isNative = classFileMethodImpl.isMethodNative(this.methodSignatureResolved);
        } catch (MethodNotFoundException e) {
            //it is still possible that the method
            //has a meta-level implementation
            this.classFileMethodImpl = null;
            this.isNative = false;
        } catch (ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }

        //builds a signature for the method implementation;
        //falls back to the signature of the resolved method
        //if there is no base-level implementation
        this.methodSignatureImpl = (this.classFileMethodImpl == null ? this.methodSignatureResolved : 
            new Signature(this.classFileMethodImpl.getClassName(), 
                          this.methodSignatureResolved.getDescriptor(), 
                          this.methodSignatureResolved.getName()));
    }
}
