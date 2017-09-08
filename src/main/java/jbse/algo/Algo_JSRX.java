package jbse.algo;

import static jbse.bc.Offsets.JSR_OFFSET;
import static jbse.bc.Offsets.JSR_W_OFFSET;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;

/**
 * {@link Algorithm} for the "jump subroutine"
 * bytecodes (jsr, jsr_w).
 * 
 * @author Pietro Braione
 */
final class Algo_JSRX extends Algorithm<
BytecodeData_1ZOF,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final boolean wide; //set by the constructor

    /**
     * Constructor.
     * 
     * @param wide {@code true} for jsr_w, {@code false} for jsr.
     */
    public Algo_JSRX(boolean wide) {
        this.wide = wide;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_1ZOF> bytecodeData() {
        return () -> BytecodeData_1ZOF.withFarOffset(this.wide).get();
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
            final int programCounter = state.getPC();
            final int bytecodeOffset = (this.wide ? JSR_W_OFFSET : JSR_OFFSET);
            state.pushOperand(state.getCalculator().valInt(programCounter + bytecodeOffset));
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> this.data.jumpOffset();
    }
}