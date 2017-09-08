package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_WIDE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_OFFSET;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Value;

/**
 * Algorithm managing all the *store (store into local variable) bytecodes 
 * ([a/d/f/i/l]store).
 * 
 * @author Pietro Braione
 */
final class Algo_XSTORE extends Algorithm<
BytecodeData_1UX,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_1UX> bytecodeData() {
        return BytecodeData_1UX::get;
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
            final Value valTemp = this.data.operand(0);
            final int index = (this.data.nextWide() ? this.data.immediateUnsignedWord() : this.data.immediateUnsignedByte());
            try {
                state.setLocalVariable(index, valTemp);
            } catch (InvalidSlotException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
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
        return () -> (this.data.nextWide() ? XLOADSTORE_IMMEDIATE_WIDE_OFFSET : XLOADSTORE_IMMEDIATE_OFFSET);
    }
}
