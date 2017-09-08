package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.CASTINSTANCEOF_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Abstract {@link Algorithm} implementing the 
 * checkcast and the instanceof bytecodes.
 * 
 * @author Pietro Braione
 *
 */
abstract class Algo_CASTINSTANCEOF extends Algorithm<
BytecodeData_1CL,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    boolean isSubclass; //result of the check, for the subclasses of this algorithm

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
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
                final ClassHierarchy hier = state.getClassHierarchy();
                final String currentClassName = state.getCurrentMethodSignature().getClassName();    
                hier.resolveClass(currentClassName, this.data.className());
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
                //gets the operand
                final Reference tmpValue = (Reference) this.data.operand(0);

                //checks whether the object's class is a subclass 
                //of the class name from the constant pool
                final boolean isSubclass;
                if (state.isNull(tmpValue)) {
                    isSubclass = true;  //the null value belongs to all classes
                } else {
                    final Objekt objS = state.getObject(tmpValue);
                    String classS = objS.getType();
                    isSubclass = state.getClassHierarchy().isSubclass(classS, this.data.className());
                }

                //completes the bytecode semantics
                complete(state, isSubclass);

            } catch (ClassCastException e) {
                throwVerifyError(state);
            } 
        };
    }

    protected abstract void complete(State state, boolean isSubclass) 
    throws InterruptException;

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> CASTINSTANCEOF_OFFSET;
    }
}
