package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.MATH_LOGICAL_OP_OFFSET;
import static jbse.bc.Signatures.ARITHMETIC_EXCEPTION;
import static jbse.common.Type.isPrimitiveIntegralOpStack;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

//TODO this implementation of division works only for concrete values, add the case for symbolic ones with multiple successors
/**
 * {@link Algorithm} for all the *rem bytecodes
 * ([i/l/f/d]rem).
 * 
 * @author Pietro Braione
 */
final class Algo_XREM extends Algorithm<
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
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final Primitive val1 = (Primitive) this.data.operand(0);
                final Primitive val2 = (Primitive) this.data.operand(1);
                if (isPrimitiveIntegralOpStack(val2.getType())) {
                    if (val2 instanceof Simplex) {
                        final Simplex op0_S = (Simplex) val2;
                        if (op0_S.isZeroOne(true)) {
                            throwNew(state, ARITHMETIC_EXCEPTION);
                            return;
                        }
                    }
                }
                state.pushOperand(val1.rem(val2));
            } catch (ClassCastException | InvalidTypeException | InvalidOperandException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> MATH_LOGICAL_OP_OFFSET;
    }
}
