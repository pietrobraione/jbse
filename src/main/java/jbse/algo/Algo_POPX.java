package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.POP_OFFSET;
import static jbse.common.Type.isCat_1;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Value;

/**
 * {@link Algorithm} managing the pop* bytecodes
 * (pop, pop2).
 * 
 * @author Pietro Braione
 */
final class Algo_POPX extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final boolean cat1; //set by constructor

    /**
     * Constructor.
     * 
     * @param cat1 {@code true} for pop, {@code false} for pop2.
     */
    public Algo_POPX(boolean cat1) {
        this.cat1 = cat1;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1; //one sure operand, but it can pop up to 2 operands
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            if (this.cat1 && !isCat_1(this.data.operand(0).getType())) {
                throwVerifyError(state);
                exitFromAlgorithm();
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
            if (!this.cat1 && isCat_1(this.data.operand(0).getType())) {
                try {
                    //pops and checks the second operand
                    final Value secondPopped = state.popOperand();
                    if (!isCat_1(secondPopped.getType())) {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                } catch (InvalidNumberOfOperandsException e) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> POP_OFFSET;
    }
}
