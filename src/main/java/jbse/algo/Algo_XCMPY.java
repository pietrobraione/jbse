package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XCMPY_OFFSET;
import static jbse.bc.Opcodes.OP_IFEQ;
import static jbse.bc.Opcodes.OP_IFGE;
import static jbse.bc.Opcodes.OP_IFGT;
import static jbse.bc.Opcodes.OP_IFLE;
import static jbse.bc.Opcodes.OP_IFLT;
import static jbse.bc.Opcodes.OP_IFNE;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.val.Calculator;
import jbse.val.Primitive;

/**
 * {@link Algorithm} managing all the *cmp* bytecodes ([d/f/l]cmp[g/l]). 
 * It decides over a comparison between primitives (longs, 
 * doubles, floats), a sheer numeric decision.
 * This implementation detects whether the faster 
 * {@link Algo_XCMPY_FAST} implementation can be used 
 * instead of its own, and in such case redispatches the
 * call to the latter.
 * 
 * @author Pietro Braione
 */
final class Algo_XCMPY extends Algorithm<
BytecodeData_0,
DecisionAlternative_XCMPY,
StrategyDecide<DecisionAlternative_XCMPY>,
StrategyRefine<DecisionAlternative_XCMPY>,
StrategyUpdate<DecisionAlternative_XCMPY>> {

    /**
     * Faster, but not always applicable, alternative 
     * implementation of the bytecode.
     */
    private final Algo_XCMPY_FAST algo_XCMPY_FAST = new Algo_XCMPY_FAST();

    private Primitive val1, val2; //set by cook

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
        return (state) -> {
            try {
                try {
                    this.val1 = (Primitive) this.data.operand(0);
                    this.val2 = (Primitive) this.data.operand(1);
                } catch (ClassCastException e) {
                    throwVerifyError(state, this.ctx.getCalculator());
                    exitFromAlgorithm();
                }

                //check 
                final int nextBytecode = state.getInstruction(1);
                final boolean fast = (nextBytecode == OP_IFEQ ||
                                      nextBytecode == OP_IFGE || 
                                      nextBytecode == OP_IFGT ||
                                      nextBytecode == OP_IFLE ||
                                      nextBytecode == OP_IFLT ||
                                      nextBytecode == OP_IFNE);
                if (fast) {
                    continueWith(this.algo_XCMPY_FAST);
                }
            } catch (InvalidProgramCounterException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_XCMPY> classDecisionAlternative() {
        return DecisionAlternative_XCMPY.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_XCMPY> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_XCMPY(this.val1, this.val2, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_XCMPY> refiner() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
        	state.assume(calc.simplify(this.ctx.decisionProcedure.simplify(calc.push(this.val1).applyBinary(alt.operator(), this.val2).pop())));
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_XCMPY> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            state.pushOperand(calc.valInt(alt.value()));
            try {
                state.incProgramCounter(XCMPY_OFFSET);
            } catch (InvalidProgramCounterException e) {
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
        return () -> XCMPY_OFFSET;
    }
}