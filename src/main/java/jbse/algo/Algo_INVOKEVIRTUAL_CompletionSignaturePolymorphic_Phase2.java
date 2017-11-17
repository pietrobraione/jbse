package jbse.algo;

import static jbse.algo.BytecodeData_1KME.Kind.kind;
import static jbse.algo.Continuations.invokestatic;
import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_FROMMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;
import jbse.val.exc.InvalidTypeException;

/**
 * Algorithm for completing the semantics of the 
 * invokevirtual bytecode for signature
 * polymorphic methods. This is the phase 2 (adaptation
 * of the {@link java.lang.invoke.MethodHandle} object
 * to the {@link java.lang.invoke.MethodType}).
 *  
 * @author Pietro Braione
 */
final class Algo_INVOKEVIRTUAL_CompletionSignaturePolymorphic_Phase2<D extends BytecodeData> extends Algo_INVOKEX_Completion<D> {
    public Algo_INVOKEVIRTUAL_CompletionSignaturePolymorphic_Phase2(Supplier<D> bytecodeData) {
        super(false, false, false, bytecodeData); //only invokevirtual can invoke signature polymorphic methods
    }

    private int pcOffsetReturn; //set by methods
    
    public void setPcOffset(int pcOffset) {
        this.pcOffsetReturn = pcOffset;
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //TODO
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
                this.ctx.nativeInvoker.doInvokeNative(state, this.methodSignatureResolved, this.data.operands(), this.pcOffsetReturn);
                exitFromAlgorithm();
            }

            //otherwise, pushes a new frame for the method
            try {
                state.pushFrame(this.methodSignatureImpl, false, this.pcOffsetReturn, this.data.operands());
            } catch (InvalidProgramCounterException | InvalidSlotException | InvalidTypeException e) {
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
