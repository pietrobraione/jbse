package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_REFINEMAPCOMPLETE;
import static jbse.common.Type.INT;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.bc.Snippet;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Instance;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_IFX;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_LINKEDMAP#refineOnFreshEntryAndBranch()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH extends Algo_INVOKEMETA<
DecisionAlternative_IFX,
StrategyDecide<DecisionAlternative_IFX>, 
StrategyRefine<DecisionAlternative_IFX>, 
StrategyUpdate<DecisionAlternative_IFX>> {
	private ReferenceSymbolic thisReference;
	private Instance map;
	
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
            	this.thisReference = (ReferenceSymbolic) this.data.operand(0);
                if (state.isNull(this.thisReference)) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base.JAVA_LINKEDMAP.refineOnFreshEntryAndBranch method is null.");
                }
                this.map = (Instance) state.getObject(this.thisReference);
                if (this.map == null) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base.JAVA_LINKEDMAP.refineOnFreshEntryAndBranch method is symbolic and unresolved.");
                }
            } catch (ClassCastException e) {
                throwVerifyError(state, calc);
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
        	final Calculator calc = this.ctx.getCalculator();
        	Primitive condition = null; //to keep the compiler happy
			try {
				condition = calc.pushAny().widen(INT).eq(calc.valInt(0)).pop();
			} catch (NoSuchElementException | InvalidOperandException | InvalidTypeException e) {
				//this should never happen
				failExecution(e);
			}
            final Outcome o = this.ctx.decisionProcedure.decide_IFX(condition, result);
            return Outcome.val(true, o.branchingDecision()); //tweak to trigger refinement
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_IFX> refiner() {
        return (state, alt) -> {
    		try {
    			final Snippet snippet;
    			if (alt.value()) {
        			final ReferenceSymbolic key = state.createSymbolMemberMapKey(this.map.getOrigin());
        			final ReferenceSymbolic value = state.createSymbolMemberMapValueKeyInitialHistoryPoint(this.map.getOrigin(), key);
    				state.pushOperand(this.thisReference);
    				state.pushOperand(key);
    				state.pushOperand(value);
    				snippet = state.snippetFactoryWrap()
    						.op_invokevirtual(JBSE_JAVA_LINKEDMAP_REFINEIN)
    						.op_return()
    						.mk();
    			} else {
    				state.pushOperand(this.thisReference);
    				snippet = state.snippetFactoryWrap()
    						.op_invokevirtual(JBSE_JAVA_LINKEDMAP_REFINEMAPCOMPLETE)
    						.op_return()
    						.mk();
    			}
    			state.pushSnippetFrameWrap(snippet, INVOKESPECIALSTATICVIRTUAL_OFFSET);
    			exitFromAlgorithm();
			} catch (ThreadStackEmptyException | InvalidProgramCounterException e) {
				//this should never happen
				failExecution(e);
			}
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_IFX> updater() {
        return (state, alt) -> {
        	//nothing to do
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true; //irrelevant
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET; //irrelevant
    }
}
