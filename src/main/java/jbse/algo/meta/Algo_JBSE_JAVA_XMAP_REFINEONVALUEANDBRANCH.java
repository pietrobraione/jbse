package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
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
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#refineOnValueAndBranch(Object)}.
 * 
 * @author Pietro Braione
 */
public abstract class Algo_JBSE_JAVA_XMAP_REFINEONVALUEANDBRANCH extends Algo_INVOKEMETA<
DecisionAlternative_IFX,
StrategyDecide<DecisionAlternative_IFX>, 
StrategyRefine<DecisionAlternative_IFX>, 
StrategyUpdate<DecisionAlternative_IFX>> {
	private final String modelClassName;     //set by constructor
	private final Signature containsValue;   //set by constructor
	private final Signature refineIn;        //set by constructor
	private final Signature refineOutValue;  //set by constructor
	private ReferenceSymbolic thisReference; //set by cooker
	private Reference valueReference;        //set by cooker
	private Instance map;                    //set by cooker
	private Primitive valueIn, valueOut;     //set by cooker
	
	public Algo_JBSE_JAVA_XMAP_REFINEONVALUEANDBRANCH(String modelClassName, Signature containsValue, Signature refineIn, Signature refineOutValue) {
		this.modelClassName = modelClassName;
		this.containsValue = containsValue;
		this.refineIn = refineIn;
		this.refineOutValue = refineOutValue;
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
                    failExecution("The 'this' parameter to jbse.base." + this.modelClassName + ".refineOnValueAndBranch method is null.");
                }
                this.map = (Instance) state.getObject(this.thisReference);
                if (this.map == null) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base." + this.modelClassName + ".refineOnValueAndBranch method is symbolic and unresolved.");
                }
                this.valueReference = (Reference) this.data.operand(1);
                final Primitive javaMapContainsValue = calc.applyFunctionPrimitive(BOOLEAN, state.getHistoryPoint(), this.containsValue.toString(), this.thisReference, this.valueReference).widen(INT).pop();
                this.valueIn = calc.push(javaMapContainsValue).ne(calc.valInt(0)).pop();
                this.valueOut = calc.push(javaMapContainsValue).eq(calc.valInt(0)).pop();
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
            final Outcome o = this.ctx.decisionProcedure.decide_IFX(this.valueIn, result);
            return o;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_IFX> refiner() {
        return (state, alt) -> {
            state.assume(alt.value() ? this.valueIn : this.valueOut);
            
    		try {
    			final Snippet snippet;
    			if (alt.value()) {
    				final ReferenceSymbolic key = state.createSymbolMemberMapKeyHistoryPointCurrent(this.map.getOrigin(), this.valueReference);
    				state.pushOperand(this.thisReference);
    				state.pushOperand(key);
    				state.pushOperand(this.valueReference);
    				snippet = state.snippetFactoryWrap()
    						.op_invokevirtual(this.refineIn)
    						.op_return()
    						.mk();
    			} else {
    				state.pushOperand(this.thisReference);
    				state.pushOperand(this.valueReference);
    				snippet = state.snippetFactoryWrap()
    						.op_invokevirtual(this.refineOutValue)
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
