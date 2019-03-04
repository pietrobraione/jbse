package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.IFX_OFFSET;
import static jbse.common.Type.INT;
import static jbse.common.Type.widens;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.tree.DecisionAlternative_IFX;
import jbse.val.Calculator;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} managing all the "branch if integer comparison" bytecodes 
 * (if[eq/ge/gt/le/lt/ne], if_icmp[eq/ge/gt/le/lt/ne]). It decides over
 * the branch to be taken, a sheer numeric decision.
 * 
 * @author Pietro Braione
 */
final class Algo_IFX extends Algorithm<
BytecodeData_1ON,
DecisionAlternative_IFX,
StrategyDecide<DecisionAlternative_IFX>,
StrategyRefine<DecisionAlternative_IFX>,
StrategyUpdate<DecisionAlternative_IFX>> {

    private final boolean compareWithZero; //set by the constructor
    private final Operator operator; //set by the constructor

    public Algo_IFX(boolean compareWithZero, Operator operator) {
        this.compareWithZero = compareWithZero;
        this.operator = operator;
    }

    private Primitive comparison; //produced by cooker
    private boolean doJump; //produced by updater

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> (this.compareWithZero ? 1 : 2);
    }

    @Override
    protected Supplier<BytecodeData_1ON> bytecodeData() {
        return BytecodeData_1ON::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            //gets the operands
        	final Calculator calc = this.ctx.getCalculator();
            Primitive val1 = null, val2 = null; //to keep the compiler happy
            try {
                val1 = (Primitive) this.data.operand(0);
                if (this.compareWithZero) {
                    val2 = calc.valInt(0);
                    //the next conversion is necessary because  
                    //Algo_XCMPY_FAST spills nonint values 
                    //to the operand stack.
                    if (widens(val1.getType(), INT)) {
                        val2 = calc.push(val2).to(val1.getType()).pop();
                    } else {
                        val1 = calc.push(val1).to(val2.getType()).pop();
                    }
                } else {
                    val2 = (Primitive) this.data.operand(1);
                }
            } catch (ClassCastException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //builds the comparison condition
            try {
                this.comparison = calc.push(val1).applyBinary(this.operator, val2).pop();
            } catch (InvalidOperandException | InvalidTypeException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_IFX> classDecisionAlternative() {
        return DecisionAlternative_IFX.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_IFX> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_IFX(this.comparison, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_IFX> refiner() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            final Primitive assumption = (alt.value() ? this.comparison : calc.push(this.comparison).not().pop());
            state.assume(calc.simplify(this.ctx.decisionProcedure.simplify(assumption)));
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_IFX> updater() {
        return (state, alt) -> {
            this.doJump = alt.value();
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> (this.doJump ? this.data.jumpOffset() : IFX_OFFSET);
    }
}