package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.NEW_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;

import java.util.function.Supplier;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;

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
            //performs resolution
            try {
                final String currentClassName = state.getCurrentMethodSignature().getClassName();
                state.getClassHierarchy().resolveClass(currentClassName, this.data.className());
            } catch (ClassFileNotFoundException e) {
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
            
            //possibly creates and initializes the class
            try {
                ensureClassCreatedAndInitialized(state, this.data.className(), this.ctx);
            } catch (InvalidInputException | BadClassFileException e) {
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
            //creates the new object in the heap
            state.pushOperand(state.createInstance(this.data.className()));
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