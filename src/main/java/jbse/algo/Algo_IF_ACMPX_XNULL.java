package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.IF_ACMPX_XNULL_OFFSET;
import static jbse.mem.Util.areAlias;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_IFX;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * {@link Algorithm} managing all the "branch if reference comparison" 
 * bytecodes, including comparison with null (if_acmp[eq/ne], ifnull, ifnonnull). 
 * 
 * @author Pietro Braione
 *
 */
final class Algo_IF_ACMPX_XNULL extends Algorithm<
BytecodeData_1ON,
DecisionAlternative_IFX,  //does not really need a decision alternative, but it's necessary to mark that this is a branching bytecode
StrategyDecide<DecisionAlternative_IFX>,
StrategyRefine<DecisionAlternative_IFX>,
StrategyUpdate<DecisionAlternative_IFX>> {

    private final boolean compareWithNull; //set by constructor
    private final boolean compareForEquality; //set by constructor

    /**
     * Constructor.
     * 
     * @param compareWithNull 
     *        {@code true} for ifnull and ifnonnull, 
     *        {@code false} for if_acmp[eq/ne].
     * @param compareForEquality 
     *        {@code true} for equality check 
     *        (if_acmpeq, ifnull), {@code false} 
     *        otherwise (if_acmpne, ifnonnull).
     */
    public Algo_IF_ACMPX_XNULL(boolean compareWithNull, boolean compareForEquality) {
        this.compareWithNull = compareWithNull;
        this.compareForEquality = compareForEquality;
    }

    boolean doJump; //produced by updater

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> (this.compareWithNull ? 1 : 2);
    }

    @Override
    protected Supplier<BytecodeData_1ON> bytecodeData() {
        return BytecodeData_1ON::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            //gets the values to compare
            Reference val1 = null, val2 = null; //to keep the compiler happy
            try {
                if (this.compareWithNull) {
                    val1 = Null.getInstance();
                    val2 = (Reference) this.data.operand(0);
                } else {
                    val1 = (Reference) this.data.operand(0);
                    val2 = (Reference) this.data.operand(1);
                }
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }

            //computes branch condition by comparing val1 and
            //val2 (note that both are resolved as they come
            //from the operand stack)
            this.doJump = 
               (this.compareForEquality ? areAlias(state, val1, val2) :  //also true when both are null
                !areAlias(state, val1, val2));
        };
    }

    @Override
    protected Class<DecisionAlternative_IFX> classDecisionAlternative() {
        return DecisionAlternative_IFX.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_IFX> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_IFX.toConcrete(this.doJump));
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_IFX> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_IFX> updater() {
        return (state, alt) -> { };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> (this.doJump ? this.data.jumpOffset() : IF_ACMPX_XNULL_OFFSET);
    }
}