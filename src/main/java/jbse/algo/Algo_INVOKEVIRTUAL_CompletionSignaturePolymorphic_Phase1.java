package jbse.algo;

import static jbse.algo.Continuations.invokestatic;
import static jbse.algo.Continuations.patchCode;
import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Opcodes.OP_INVOKEVIRTUAL_SIGNATUREPOLYMORPHIC_PHASE2;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.JAVA_METHODTYPE_FROMMETHODDESCRIPTORSTRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Null;

/**
 * Algorithm for completing the semantics of the 
 * invokevirtual bytecode for signature
 * polymorphic methods. This is the phase 1 (creation
 * of the {@link java.lang.invoke.MethodType} object).
 * Note that this algorithm needs to patch the current
 * state's current bytecode so that phase 2
 * is executed after the object is created.
 *  
 * @author Pietro Braione
 */
final class Algo_INVOKEVIRTUAL_CompletionSignaturePolymorphic_Phase1<D extends BytecodeData> extends Algo_INVOKEX_Completion<D> {
    private final Algo_INVOKEVIRTUAL_CompletionSignaturePolymorphic_Phase2<D> algo_INVOKEX_CompletionSignaturePolymorphic_Phase2 = new Algo_INVOKEVIRTUAL_CompletionSignaturePolymorphic_Phase2<D>(bytecodeData());
    
    public Algo_INVOKEVIRTUAL_CompletionSignaturePolymorphic_Phase1(Supplier<D> bytecodeData) {
        super(false, false, false, bytecodeData); //only invokevirtual can invoke signature polymorphic methods
    }

    private int pcOffsetReturn; //set by methods
    
    public void setPcOffset(int pcOffset) {
        this.pcOffsetReturn = pcOffset;
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //resolves the method type
            final ClassHierarchy hier = state.getClassHierarchy();
            try {
                final String currentClass = state.getCurrentMethodSignature().getClassName();
                hier.resolveMethodType(currentClass, this.data.signature().getDescriptor());
            } catch (ClassFileNotFoundException e) {
                //TODO is it ok?
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                //TODO is it ok?
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileException e) {
                //this should never happen since we already caught both its subclasses
                failExecution(e);
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }

            //prepares the algorithm for phase 2
            this.algo_INVOKEX_CompletionSignaturePolymorphic_Phase2.setPcOffset(this.pcOffsetReturn);
            
            //performs an upcall to java.lang.invoke.MethodType.fromMethodDescriptorString
            try {
                state.ensureStringLiteral(this.data.signature().getDescriptor());
                state.pushOperand(state.referenceToStringLiteral(this.data.signature().getDescriptor()));
                state.pushOperand(Null.getInstance());
                continueWith(patchCode(OP_INVOKEVIRTUAL_SIGNATUREPOLYMORPHIC_PHASE2, this.algo_INVOKEX_CompletionSignaturePolymorphic_Phase2),
                             invokestatic(JAVA_METHODTYPE_FROMMETHODDESCRIPTORSTRING, 0));
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return null; //never used
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return null; //never used
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return null; //never used
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return null; //never used
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return null; //never used
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return null; //never used
    }
}
