package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.GETX_PUTX_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_FIELD_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;

//TODO extract common superclass with Algo_GETX and eliminate duplicate code
abstract class Algo_PUTX extends Algorithm<
BytecodeData_1FI,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    
    protected Signature fieldSignatureResolved; //set by cook
	
    @Override
    protected final Supplier<BytecodeData_1FI> bytecodeData() {
        return BytecodeData_1FI::get;
    }
    
    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //gets the class hierarchy
            final ClassHierarchy hier = state.getClassHierarchy();

            //performs field resolution
            String currentClassName = null; //it's final 
            try {
                currentClassName = state.getCurrentMethodSignature().getClassName();    
                this.fieldSignatureResolved = hier.resolveField(currentClassName, this.data.signature());
            } catch (ClassFileNotFoundException e) {
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (FieldNotFoundException e) {
                throwNew(state, NO_SUCH_FIELD_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException | FieldNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }

            //checks the field
            try {
                check(state, currentClassName);
            } catch (FieldNotFoundException | BadClassFileException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
    
    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            put(state);
        };
    }
    
    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }
    
    @Override
    protected final StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }
    
    protected abstract void check(State state, String currentClass)
    throws FieldNotFoundException, BadClassFileException, 
    DecisionException, ClasspathException, InterruptException;

    protected abstract void put(State state)
    throws InterruptException;
    
    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }
    
    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> GETX_PUTX_OFFSET;
    }
}
