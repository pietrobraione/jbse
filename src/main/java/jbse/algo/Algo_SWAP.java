package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.SWAP_OFFSET;
import static jbse.common.Type.isCat_1;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;

/**
 * {@link Algorithm} handling the swap bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_SWAP extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
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
        return (state, alt) -> { 
            if (!isCat_1(this.data.operand(0).getType()) || !isCat_1(this.data.operand(1).getType())) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.data.operand(1));
            state.pushOperand(this.data.operand(0));
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> SWAP_OFFSET;
    }
}
