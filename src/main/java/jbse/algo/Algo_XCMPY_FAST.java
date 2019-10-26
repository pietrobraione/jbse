package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XCMPY_OFFSET;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.LONG;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * {@link Algorithm} managing all the *cmp* bytecodes ([d/f/l]cmp[g/l]). 
 * It decides over a comparison between primitives (longs, 
 * doubles, floats), a sheer numeric decision.
 * This implementation is for the (common) case where 
 * the next bytecode is a two-way if bytecode, avoiding the
 * creation of a three-way branch in the symbolic execution tree.
 * 
 * @author Pietro Braione
 */
final class Algo_XCMPY_FAST extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>,
StrategyRefine<DecisionAlternative_NONE>,
StrategyUpdate<DecisionAlternative_NONE>> {
    Primitive valToPush; //set by cooker

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
            final Primitive val1 = (Primitive) this.data.operand(0);
            final Primitive val2 = (Primitive) this.data.operand(1);
            
            if (val1 instanceof Simplex && val2 instanceof Simplex) {
                //if both values are Simplex, do not push difference
                //because it may overflow
                final Simplex val1Simplex = (Simplex) val1;
                final Simplex val2Simplex = (Simplex) val2;
                if (val1.getType() == DOUBLE && val2.getType() == DOUBLE) {
                    final double val1Double = ((Double) val1Simplex.getActualValue()).doubleValue();
                    final double val2Double = ((Double) val2Simplex.getActualValue()).doubleValue();
                    if (val1Double > val2Double) {
                        this.valToPush = this.ctx.getCalculator().valInt(1);
                    } else if (val1Double == val2Double) {
                        this.valToPush = this.ctx.getCalculator().valInt(0);
                    } else {
                        this.valToPush = this.ctx.getCalculator().valInt(-1);
                    }
                } else if (val1.getType() == FLOAT && val2.getType() == FLOAT) {
                    final float val1Float = ((Float) val1Simplex.getActualValue()).floatValue();
                    final float val2Float = ((Float) val2Simplex.getActualValue()).floatValue();
                    if (val1Float > val2Float) {
                        this.valToPush = this.ctx.getCalculator().valInt(1);
                    } else if (val1Float == val2Float) {
                        this.valToPush = this.ctx.getCalculator().valInt(0);
                    } else {
                        this.valToPush = this.ctx.getCalculator().valInt(-1);
                    }
                } else if (val1.getType() == LONG && val2.getType() == LONG) {
                    final long val1Long = ((Long) val1Simplex.getActualValue()).longValue();
                    final long val2Long = ((Long) val2Simplex.getActualValue()).longValue();
                    if (val1Long > val2Long) {
                        this.valToPush = this.ctx.getCalculator().valInt(1);
                    } else if (val1Long == val2Long) {
                        this.valToPush = this.ctx.getCalculator().valInt(0);
                    } else {
                        this.valToPush = this.ctx.getCalculator().valInt(-1);
                    }
                } else {
                    throwVerifyError(state, this.ctx.getCalculator());
                    exitFromAlgorithm();
                }
            } else {
                //if at least one of val1 and val2 is symbolic,
                //pushes the difference between val1 and val2
                //(checking it is equivalent to checking the 
                //true outcome of the bytecode). Note that this
                //works only in the theory of true numbers, not
                //in the theory of bitvectors!
                //TODO cope with overflow!
                this.valToPush = this.ctx.getCalculator().push(val1).sub(val2).pop();
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
            state.pushOperand(this.valToPush);
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