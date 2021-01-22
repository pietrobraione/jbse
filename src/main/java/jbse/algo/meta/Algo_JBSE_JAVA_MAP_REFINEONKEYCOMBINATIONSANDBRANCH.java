package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.JAVA_MAP_CONTAINSKEY;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEIN;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_REFINEOUTKEY;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.INT;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.Snippet;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_JAVA_MAP;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link jbse.base.JAVA_MAP#refineOnKeyCombinationsAndBranch(Object...)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH extends Algo_INVOKEMETA<
DecisionAlternative_JAVA_MAP,
StrategyDecide<DecisionAlternative_JAVA_MAP>, 
StrategyRefine<DecisionAlternative_JAVA_MAP>, 
StrategyUpdate<DecisionAlternative_JAVA_MAP>> {
	private ReferenceSymbolic thisReference;
	private Reference keysReference;
	private Instance map;
	private int numKeys;
	private Primitive[] keyPredicates;
	
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
            	this.thisReference = (ReferenceSymbolic) this.data.operand(0);
                if (state.isNull(this.thisReference)) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base.JAVA_MAP.refineOnKeyCombinationsAndBranch method is null.");
                }
                this.map = (Instance) state.getObject(this.thisReference);
                if (this.map == null) {
                    //this should never happen
                    failExecution("The 'this' parameter to jbse.base.JAVA_MAP.refineOnKeyCombinationsAndBranch method is symbolic and unresolved.");
                }
                this.keysReference = (Reference) this.data.operand(1);
                if (state.isNull(this.keysReference)) {
                    //this should never happen
                    failExecution("The 'keys' parameter to jbse.base.JAVA_MAP.refineOnKeyCombinationsAndBranch method is null.");
                }
                final Array keysArray = (Array) state.getObject(this.keysReference);
                if (keysArray == null) {
                    //this should never happen
                    failExecution("The 'keys' parameter to jbse.base.JAVA_MAP.refineOnKeyCombinationsAndBranch method is symbolic and unresolved.");
                }
                if (keysArray.isSymbolic() || !keysArray.hasSimpleRep()) {
                    //this should never happen
                    failExecution("The 'keys' parameter to jbse.base.JAVA_MAP.refineOnKeyCombinationsAndBranch method is a symbolic array, or has not a simple representation.");
                }
                this.numKeys = ((Integer) ((Simplex) keysArray.getLength()).getActualValue()).intValue();
                if (this.numKeys > 10) { //TODO make this limit parametric
                	throw new MetaUnsupportedException("Excessive branching of jbse.base.JAVA_MAP.refineOnKeyCombinationsAndBranch, 2^" + this.numKeys + " branches necessary.");
                }
                
                final Primitive[] keyIn = new Primitive[this.numKeys];
                final Primitive[] keyOut = new Primitive[this.numKeys];
                final Simplex zero = calc.valInt(0);
            	for (int k = 0; k < this.numKeys; ++k) {
            		final Value key = ((Array.AccessOutcomeInValue) keysArray.getFast(calc, calc.valInt(k))).getValue();
            		final Primitive javaMapContainsKey = calc.applyFunctionPrimitive(BOOLEAN, state.getHistoryPoint(), JAVA_MAP_CONTAINSKEY.toString(), this.thisReference, key).widen(INT).pop();
            		keyIn[k] = calc.push(javaMapContainsKey).ne(zero).pop();
            		keyOut[k] = calc.push(javaMapContainsKey).eq(zero).pop();
            	}
                
                final int combinations = 1 << this.numKeys;
                final Primitive truePredicate = calc.valBoolean(true);
                this.keyPredicates = new Primitive[combinations];
                for (int i = 0; i < combinations; ++i) {
                	int z = i;
                	calc.push(truePredicate);
                	for (int k = 0; k < this.numKeys; ++k) {
                		calc.and(z % 2 == 0 ? keyOut[k] : keyIn[k]);
                		z /= 2;
                	}
                	this.keyPredicates[i] = calc.pop();
                }
            } catch (ClassCastException e) {
                throwVerifyError(state, calc);
                exitFromAlgorithm();
            } catch (InvalidOperandException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
            	//this should never happen
            	failExecution(e);
			}
        };
    } 

    @Override
    protected Class<DecisionAlternative_JAVA_MAP> classDecisionAlternative() {
        return DecisionAlternative_JAVA_MAP.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_JAVA_MAP> decider() {
        return (state, result) -> {
            final Outcome o = this.ctx.decisionProcedure.decide_JAVA_MAP(this.keyPredicates, result);
            return o;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_JAVA_MAP> refiner() {
        return (state, alt) -> {
            state.assume(this.keyPredicates[alt.getBranchNumber()]);
            
    		try {
            	int z = alt.getBranchNumber();
                final Calculator calc = this.ctx.getCalculator();
                final Array keysArray = (Array) state.getObject(this.keysReference);
            	for (int k = 0; k < this.numKeys; ++k) {
            		final Reference key = (Reference) ((Array.AccessOutcomeInValue) keysArray.getFast(calc, calc.valInt(k))).getValue();
        			final Snippet snippet;
        			if (z % 2 == 0) {
        				state.pushOperand(this.thisReference);
        				state.pushOperand(key);
        				snippet = state.snippetFactoryWrap()
        						.op_invokevirtual(JBSE_JAVA_MAP_REFINEOUTKEY)
        						.op_return()
        						.mk();
        			} else {
        				final ReferenceSymbolic value = state.createSymbolMemberMapValueKeyCurrentHistoryPoint(this.map.getOrigin(), key);
        				state.pushOperand(this.thisReference);
        				state.pushOperand(key);
        				state.pushOperand(value);
        				snippet = state.snippetFactoryWrap()
        						.op_invokevirtual(JBSE_JAVA_MAP_REFINEIN)
        						.op_return()
        						.mk();
        			}
        			state.pushSnippetFrameWrap(snippet, INVOKESPECIALSTATICVIRTUAL_OFFSET);
            		z /= 2;
            	}

    			exitFromAlgorithm();
			} catch (ThreadStackEmptyException | InvalidProgramCounterException | FastArrayAccessNotAllowedException e) {
				//this should never happen
				failExecution(e);
			}
        };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_JAVA_MAP> updater() {
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
