package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.DUP_OFFSET;
import static jbse.common.Type.isCat_1;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.State;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Value;

/**
 * {@link Algorithm} for the dup*_x* (dup_x[1/2], dup2_x[1/2]) bytecodes. 
 * 
 * @author Pietro Braione
 *
 */
final class Algo_DUPX_Y extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final boolean cat1; //set by constructor
    private final boolean x1; //set by constructor

    /**
     * Constructor.
     * 
     * @param cat1 {@code true} for dup_*, {@code false} for dup2_*.
     * @param x1 {@code true} for dup*_x1, {@code false} for dup*_x2.
     */
    public Algo_DUPX_Y(boolean cat1, boolean x1) {
        this.cat1 = cat1;
        this.x1 = x1;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2; //two sure operand, but it can dup up to 4 operands
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            if (this.cat1) {
                if (!isCat_1(this.data.operand(1).getType())) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
                if (this.x1 && !isCat_1(this.data.operand(0).getType())) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
            } else if (this.x1) {
                if (isCat_1(this.data.operand(0).getType())) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
            } else if (!isCat_1(this.data.operand(0).getType()) && isCat_1(this.data.operand(0).getType())) {
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
            final Value value1 = this.data.operand(1);
            final Value value2 = this.data.operand(0);
            if (this.cat1 && this.x1) {
                dup_x1(state, value1, value2);
            } else if (this.cat1 && !this.x1) {
                if (isCat_1(value2.getType())) {
                    dup_x2_form1(state, value1, value2);
                } else {
                    dup_x2_form2(state, value1, value2);
                }
            } else if (!this.cat1 && this.x1) {
                if (isCat_1(value1.getType())) {
                    dup2_x1_form1(state, value1, value2);
                } else {
                    dup2_x1_form2(state, value1, value2);
                }
            } else {
                if (isCat_1(value1.getType()) && isCat_1(value2.getType())) {
                    dup2_x2_form1Or3(state, value1, value2);
                } else if (!isCat_1(value1.getType()) && !isCat_1(value2.getType())) {
                    dup2_x2_form4(state, value1, value2);
                } else {
                    dup2_x2_form2(state, value1, value2);
                }
            }
        };
    }

    private static void dup_x1(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException {
        state.pushOperand(value1);
        state.pushOperand(value2);
        state.pushOperand(value1);
    }

    private static void dup_x2_form1(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException, InterruptException {
        //we need a third cat1 operand
        try {
            final Value value3 = state.popOperand();
            if (!isCat_1(value3.getType())) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            state.pushOperand(value1);
            state.pushOperand(value3);
            state.pushOperand(value2);
            state.pushOperand(value1);
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    private static void dup_x2_form2(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException {
        state.pushOperand(value1);
        state.pushOperand(value2);
        state.pushOperand(value1);
    }

    private static void dup2_x1_form1(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException, InterruptException {
        //we need a third cat1 operand
        try {
            final Value value3 = state.popOperand();
            if (!isCat_1(value3.getType())) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            state.pushOperand(value2);
            state.pushOperand(value1);
            state.pushOperand(value3);
            state.pushOperand(value2);
            state.pushOperand(value1);
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    private static void dup2_x1_form2(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException {
        state.pushOperand(value1);
        state.pushOperand(value2);
        state.pushOperand(value1);
    }

    private static void dup2_x2_form1Or3(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException, InterruptException {
        //we need a third operand, that also allows us
        //to decide the form
        try {
            final Value value3 = state.popOperand();
            if (isCat_1(value3.getType())) {
                dup2_x2_form1(state, value1, value2, value3);
            } else {
                dup2_x2_form3(state, value1, value2, value3);
            }
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    private static void dup2_x2_form1(State state, Value value1, Value value2, Value value3) 
    throws ThreadStackEmptyException, InterruptException {
        try {
            //we need a fourth cat1 operand
            final Value value4 = state.popOperand();
            if (!isCat_1(value3.getType())) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            state.pushOperand(value2);
            state.pushOperand(value1);
            state.pushOperand(value4);
            state.pushOperand(value3);
            state.pushOperand(value2);
            state.pushOperand(value1);
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    private static void dup2_x2_form2(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException, InterruptException {
        //we need a third cat1 operand
        try {
            final Value value3 = state.popOperand();
            if (!isCat_1(value3.getType())) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
            state.pushOperand(value1);
            state.pushOperand(value3);
            state.pushOperand(value2);
            state.pushOperand(value1);
        } catch (InvalidNumberOfOperandsException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    private static void dup2_x2_form3(State state, Value value1, Value value2, Value value3) 
    throws ThreadStackEmptyException {
        state.pushOperand(value2);
        state.pushOperand(value1);
        state.pushOperand(value3);
        state.pushOperand(value2);
        state.pushOperand(value1);
    }

    private static void dup2_x2_form4(State state, Value value1, Value value2) 
    throws ThreadStackEmptyException {
        state.pushOperand(value1);
        state.pushOperand(value2);
        state.pushOperand(value1);
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> DUP_OFFSET;
    }
}
