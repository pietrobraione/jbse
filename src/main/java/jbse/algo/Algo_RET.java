package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Simplex;

/**
 * {@link Algorithm} managing the "return from subroutine"
 * (ret) bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_RET extends Algorithm<
BytecodeData_1LV,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private int pcReturn; //set by updater

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_1LV> bytecodeData() {
        return BytecodeData_1LV::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        //TODO check that this.data.localVariableValue() is Simplex and integer, and when it is symbolic throw a CannotManageStateException, possibly SymbolicValueNotAllowedException.
        return (state) -> { };
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
                final Simplex localVariableValueAsSimplex = (Simplex) this.data.localVariableValue();
                this.pcReturn = ((Integer) localVariableValueAsSimplex.getActualValue()).intValue();
            } catch (ClassCastException e) {
                throwVerifyError(state);
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> false;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> this.pcReturn;
    }
}
