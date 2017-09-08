package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ABSTRACT_METHOD_ERROR;

import java.util.function.Supplier;

import jbse.algo.exc.CannotInvokeNativeException;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Algorithm for completing the semantics of the 
 * invoke* bytecodes
 * (invoke[interface/special/static/virtual]).
 *  
 * @author Pietro Braione
 */
final class Algo_INVOKEX_Completion extends Algo_INVOKEX_Abstract {

    public Algo_INVOKEX_Completion(boolean isInterface, boolean isSpecial, boolean isStatic) {
        super(isInterface, isSpecial, isStatic);
    }

    private int pcOffsetReturn; //set by cooker

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //performs method resolution
            try {
                resolveMethod(state);
            } catch (IncompatibleClassFileException | 
                     MethodAbstractException | 
                     MethodNotFoundException | 
                     MethodNotAccessibleException | 
                     BadClassFileException e) {
                //this should never happen (Algo_INVOKEX already checked them)
                failExecution(e);
            }

            //looks for the method implementation, and determines
            //whether the implementation is native
            try {
                findImplAndCalcNative(state);
            } catch (IncompatibleClassFileException | 
                     NullPointerException | 
                     BadClassFileException e) {
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

            //if the method has no implementation, raises AbstractMethodError
            try {
                if (this.classFileMethodImpl == null || this.classFileMethodImpl.isMethodAbstract(this.methodSignatureImpl)) {
                    throwNew(state, ABSTRACT_METHOD_ERROR);
                    exitFromAlgorithm();
                }
            } catch (MethodNotFoundException e) {
                //this should never happen after resolution 
                failExecution(e);
            }     

            //sets the program counter offset for the return point
            this.pcOffsetReturn = (this.isInterface ? 
                INVOKEDYNAMICINTERFACE_OFFSET : 
                INVOKESPECIALSTATICVIRTUAL_OFFSET);
        };
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //if the method is native, delegates the responsibility 
            //to the native invoker
            if (this.isNative) {
                try {
                    this.ctx.nativeInvoker.doInvokeNative(state, this.methodSignatureResolved, this.data.operands(), this.pcOffsetReturn);
                } catch (CannotInvokeNativeException e) {
                    failExecution(e);
                }
                exitFromAlgorithm();
            }

            //otherwise, pushes a new frame for the method
            try {
                state.pushFrame(this.methodSignatureImpl, false, this.pcOffsetReturn, this.data.operands());
            } catch (InvalidProgramCounterException | InvalidSlotException e) {
                //TODO is it ok?
                throwVerifyError(state);
            } catch (NullMethodReceiverException | BadClassFileException | 
                     MethodNotFoundException | MethodCodeNotFoundException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}
