package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.JAVA_MAP_CONTAINSKEY;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.INT;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Instance;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_IFX;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#refineOnKeyAndBranch(Object)}, 
 * {@link jbse.base.JAVA_CONCURRENTMAP#refineOnKeyAndBranch(Object)} and
 * {@link jbse.base.JAVA_LINKEDMAP#refineOnKeyAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_JBSE_JAVA_XMAP_REFINEONKEYANDBRANCH extends Algo_INVOKEMETA<
DecisionAlternative_IFX,
StrategyDecide<DecisionAlternative_IFX>, 
StrategyRefine<DecisionAlternative_IFX>, 
StrategyUpdate<DecisionAlternative_IFX>> {
	private final String modelClassName;     //set by constructor
	private final Signature refineIn;        //set by constructor
	private final Signature refineOutKey;    //set by constructor
	private ReferenceSymbolic thisReference; //set by cooker
	private Reference keyReference;          //set by cooker
	private Instance map;                    //set by cooker
	private Primitive keyIn, keyOut;         //set by cooker
	
	public Algo_JBSE_JAVA_XMAP_REFINEONKEYANDBRANCH(String modelClassName, Signature refineIn, Signature refineOutKey) {
		this.modelClassName = modelClassName;
		this.refineIn = refineIn;
		this.refineOutKey = refineOutKey;
	}
	
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
            	this.thisReference = (ReferenceSymbolic) this.data.operand(0);
                if (state.isNull(this.thisReference)) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base." + this.modelClassName + ".refineOnKeyAndBranch method is null.");
                }
                this.map = (Instance) state.getObject(this.thisReference);
                if (this.map == null) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base." + this.modelClassName + ".refineOnKeyAndBranch method is symbolic and unresolved.");
                }
                this.keyReference = (Reference) this.data.operand(1);
                final Primitive javaMapContainsKey = calc.applyFunctionPrimitive(BOOLEAN, state.getHistoryPoint(), JAVA_MAP_CONTAINSKEY.toString(), this.thisReference, this.keyReference).widen(INT).pop();
                this.keyIn = calc.push(javaMapContainsKey).ne(calc.valInt(0)).pop();
                this.keyOut = calc.push(javaMapContainsKey).eq(calc.valInt(0)).pop();
            } catch (ClassCastException e) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            } catch (InvalidOperandException | InvalidTypeException e) {
            	//this should never happen
            	failExecution(e);
			}
        };
    } 

    @Override
    protected final Class<DecisionAlternative_IFX> classDecisionAlternative() {
        return DecisionAlternative_IFX.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_IFX> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_IFX(this.keyIn, result);
            return o;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_IFX> refiner() {
        return (state, alt) -> {
            state.assume(alt.value() ? this.keyIn : this.keyOut);
            
    		try {
    			final Snippet snippet;
    			if (alt.value()) {
    				final ReferenceSymbolic value = state.createSymbolMemberMapValueKeyCurrentHistoryPoint(this.map.getOrigin(), this.keyReference);
    				state.pushOperand(this.thisReference);
    				state.pushOperand(this.keyReference);
    				state.pushOperand(value);
    				snippet = state.snippetFactoryWrap()
    						.op_invokevirtual(this.refineIn)
    						.op_return()
    						.mk();
    			} else {
    				state.pushOperand(this.thisReference);
    				state.pushOperand(this.keyReference);
    				snippet = state.snippetFactoryWrap()
    						.op_invokevirtual(this.refineOutKey)
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
    protected final StrategyUpdate<DecisionAlternative_IFX> updater() {
        return (state, alt) -> {
        	//nothing to do
        };
    }

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true; //irrelevant
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET; //irrelevant
    }
}
