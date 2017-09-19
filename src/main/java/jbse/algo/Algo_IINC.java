package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.IINC_OFFSET;
import static jbse.bc.Offsets.IINC_WIDE_OFFSET;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.InvalidSlotException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} for the iinc bytecode.
 * 
 * @author Pietro Braione
 *
 */
final class Algo_IINC extends Algorithm<
BytecodeData_2LVSX,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_2LVSX> bytecodeData() {
        return BytecodeData_2LVSX::get;
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
                final int constant = this.data.nextWide() ? this.data.immediateSignedWord() : this.data.immediateSignedByte();
                final Simplex constantSimplex = state.getCalculator().valInt(constant);
                final Primitive tmpVal = (Primitive) this.data.localVariableValue();
                state.setLocalVariable(this.data.localVariableSlot(), tmpVal.add(constantSimplex));
            } catch (ClassCastException | InvalidOperandException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (InvalidSlotException | InvalidTypeException e) {
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
        return () -> (this.data.nextWide() ? IINC_WIDE_OFFSET : IINC_OFFSET);
    }
}
