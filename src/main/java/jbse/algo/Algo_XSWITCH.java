package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} managing all the *switch bytecodes 
 * (tableswitch, lookupswitch). 
 * It decides over the branch to be taken, a sheer numeric decision.
 * 
 * @author Pietro Braione
 */
final class Algo_XSWITCH extends Algorithm<
BytecodeData_1ZSWITCH, 
DecisionAlternative_XSWITCH,
StrategyDecide<DecisionAlternative_XSWITCH>, 
StrategyRefine<DecisionAlternative_XSWITCH>, 
StrategyUpdate<DecisionAlternative_XSWITCH>> {

    private final boolean isTableSwitch; //set by constructor

    /**
     * Constructor.
     * 
     * @param isTableSwitch {@code true} for tableswitch, 
     *        {@code false} for lookupswitch.
     */
    public Algo_XSWITCH(boolean isTableSwitch) {
        this.isTableSwitch = isTableSwitch;
    }

    private Primitive selector; //set by cooker
    private int jumpOffset; //set by updater

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1; //the switch selector
    }

    @Override
    protected Supplier<BytecodeData_1ZSWITCH> bytecodeData() {
        return () -> BytecodeData_1ZSWITCH.whereTableSwitch(this.isTableSwitch).get();
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                this.selector = (Primitive) this.data.operand(0);
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_XSWITCH> classDecisionAlternative() {
        return DecisionAlternative_XSWITCH.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_XSWITCH> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_XSWITCH(state.getClassHierarchy(), this.selector, this.data.switchTable(), result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_XSWITCH> refiner() {
        return (state, alt) -> {
            //augments the path condition
            Expression branchCondition = null; //to keep the compiler happy
            try {
                final Primitive selector = (Primitive) this.data.operand(0);
                if (alt.isDefault()) {
                    branchCondition = (Expression) this.data.switchTable().getDefaultClause(selector);
                } else {  
                    final int index = alt.value();
                    final Calculator calc = state.getCalculator();
                    branchCondition = (Expression) selector.eq(calc.valInt(index));
                }
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen after call to decideSwitch
                failExecution(e);
            }
            state.assume(this.ctx.decisionProcedure.simplify(branchCondition));
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_XSWITCH> updater() {
        return (state, alt) -> {
            final SwitchTable tab = this.data.switchTable();
            if (alt.isDefault()) {
                this.jumpOffset = tab.jumpOffsetDefault();
            } else {
                final int index = alt.value();
                this.jumpOffset = tab.jumpOffset(index);
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> this.jumpOffset;
    }

    @Override
    protected void onInvalidInputException(State state, InvalidInputException e) {
        //bad selector
        throwVerifyError(state);
    }
}