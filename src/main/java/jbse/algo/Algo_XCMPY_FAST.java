package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XCMPY_OFFSET;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} managing all the *cmp* bytecodes ([d/f/l]cmp[g/l]). 
 * It decides over a comparison between primitives (longs, 
 * doubles, floats), a sheer numeric decision.
 * This implementation is for the (common) case where 
 * pushing the difference between the primitives on the 
 * operand stack yields the same semantics, but works faster.
 * 
 * @author Pietro Braione
 */
final class Algo_XCMPY_FAST extends Algorithm<
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
            //pushes the difference between val1 and val2
            //(checking it is equivalent to checking the 
            //true outcome of the bytecode) 
            try {
                final Primitive val1 = (Primitive) this.data.operand(0);
                final Primitive val2 = (Primitive) this.data.operand(1);
                state.pushOperand(val1.sub(val2));
            } catch (InvalidOperandException | InvalidTypeException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ClassCastException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> XCMPY_OFFSET;
    }
}