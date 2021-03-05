package jbse.algo;

import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwObject;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * {@link Algorithm} implementing the athrow bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_ATHROW extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
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
            try {
                final Reference myExcRef = (Reference) this.data.operand(0);
                if (state.isNull(myExcRef)) {
                    throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
                } else {
                    throwObject(state, this.ctx.getCalculator(), myExcRef);
                }
            } catch (ClassCastException e) {
                throwVerifyError(state, this.ctx.getCalculator());
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0;
    }
}




