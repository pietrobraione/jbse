package jbse.algo;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.bc.Offsets.XCONST_OFFSET;

import java.util.function.Supplier;

import jbse.common.Type;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;

/**
 * {@link Algorithm} for all the "push numeric constant" 
 * bytecodes ([d/f/i/l]const_*).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_XCONST_Y extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final char type; //set by constructor
    private final int value; //set by constructor

    /**
     * Constructor.
     * 
     * @param type the type of the constant.
     * @param value the value of the constant.
     */
    public Algo_XCONST_Y(char type, int value) {
        this.type = type;
        this.value = value;
    }

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
    	final Calculator calc = this.ctx.getCalculator();
        return (state, alt) -> {
            if (this.type == Type.INT) {
                state.pushOperand(calc.valInt(this.value));
            } else if (this.type == Type.DOUBLE) {
                state.pushOperand(calc.valDouble((double) this.value));
            } else if (this.type == Type.FLOAT) {
                state.pushOperand(calc.valFloat((float) this.value));
            } else if (this.type == Type.LONG) {
                state.pushOperand(calc.valLong((long) this.value));
            } else {
                failExecution("Const bytecode with type " + this.type + " does not exist.");
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> XCONST_OFFSET;
    }
}