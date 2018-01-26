package jbse.algo;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.NEW_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.ReferenceConcrete;
import jbse.val.exc.InvalidTypeException;

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
    
    private ReferenceConcrete newObjectReference;

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
                final ClassFile newObjectClass = state.getClassHierarchy().resolveClass(currentClass, this.data.className(), state.areStandardClassLoadersNotReady());
                
                //possibly initializes the class
                ensureClassInitialized(state, newObjectClass, this.ctx);
                
                //creates the new object in the heap
                this.newObjectReference = state.createInstance(newObjectClass);
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, e);
                exitFromAlgorithm();
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException e) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException | InvalidTypeException e) {
                throwVerifyError(state);
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
            state.pushOperand(this.newObjectReference);
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