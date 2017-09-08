package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.X2Y_OFFSET;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} implementing all the *2* bytecodes 
 * (i2[b/s/l/f/d/c], l2[i/f/d], f2[i/l/d], d2[i/l/f]).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_X2Y extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final char fromType; //set by the constructor
    private final char toType; //set by the constructor

    public Algo_X2Y(char fromType, char toType) {
        this.fromType = fromType;
        this.toType = toType;
    }

    private Primitive primitiveTo; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                final Primitive primitiveFrom  = (Primitive) this.data.operand(0);
                if (primitiveFrom.getType() != this.fromType) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
                this.primitiveTo = primitiveFrom.to(this.toType);
            } catch (ClassCastException | InvalidTypeException e) {
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
            state.pushOperand(this.primitiveTo);
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> X2Y_OFFSET;
    }
}