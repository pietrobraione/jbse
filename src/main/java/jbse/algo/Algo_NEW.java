package jbse.algo;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.NEW_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.ReferenceConcrete;

/**
 * {@link Algorithm} managing the "create new object"
 * bytecode (new).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_NEW extends Algorithm<
BytecodeData_1CL,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    
    private ClassFile newObjectClass;

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_1CL> bytecodeData() {
        return BytecodeData_1CL::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                //performs resolution
                final ClassFile currentClass = state.getCurrentClass();
                this.newObjectClass = state.getClassHierarchy().resolveClass(currentClass, this.data.className(), state.bypassStandardLoading());
                
                //possibly initializes the class
                ensureClassInitialized(state, this.ctx, this.newObjectClass);
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
                exitFromAlgorithm();
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException e) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileVersionException e) {
                throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
                exitFromAlgorithm();
            } catch (WrongClassNameException e) {
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException e) {
                throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
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
            try {
                final ReferenceConcrete newObjectReference = state.createInstance(this.ctx.getCalculator(), this.newObjectClass);
                state.pushOperand(newObjectReference);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> NEW_OFFSET;
    }
}