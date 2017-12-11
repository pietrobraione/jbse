package jbse.algo;

import java.util.function.Supplier;

import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Klass;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Algorithm handling the "return void from method"
 * (return) bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_RETURN extends Algorithm<
BytecodeData_0, 
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
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
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
            final Signature returnedMethod = state.popCurrentFrame().getCurrentMethodSignature();
            if (state.getStackSize() == 0) {
                state.setStuckReturn();
            } else {
                this.pcReturn = state.getReturnPC();
            }
            if ("<clinit>".equals(returnedMethod.getName())) {
                final Klass k = state.getKlass(returnedMethod.getClassName());
                k.setInitialized();
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
