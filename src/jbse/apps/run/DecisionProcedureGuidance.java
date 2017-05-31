package jbse.apps.run;

import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Engine;
import jbse.jvm.Runner;
import jbse.jvm.RunnerBuilder;
import jbse.jvm.Runner.Actions;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.jvm.RunnerParameters;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcome;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.Frame;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.Util;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Unresolved;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Access;
import jbse.val.AccessArrayLength;
import jbse.val.AccessArrayMember;
import jbse.val.AccessField;
import jbse.val.AccessHashCode;
import jbse.val.AccessLocalVariable;
import jbse.val.AccessStatic;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.MemoryPath;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureAlgorithms} for guided symbolic execution. It keeps 
 * a guiding {@link Engine} that must be stepped in parallel with the 
 * guided one, and filters all the decisions according to the steps 
 * done by the guiding engine.
 */
public final class DecisionProcedureGuidance extends DecisionProcedureAlgorithms {
	private final Engine engine;
	private final Frame rootFrameConcrete;
	private State initialStateConcrete;
	private final HashSet<Long> seenObjects;
	private boolean failedConcrete;
	private Exception catastrophicFailure;
	private boolean ended;
	
	/**
	 * Builds the {@link DecisionProcedureGuidance}.
	 *
	 * @param component the component {@link DecisionProcedure} it decorates.
	 * @param calc a {@link Calculator}.
	 * @param runnerParameters the {@link RunnerParameters} of the symbolic execution.
	 * @throws GuidanceException if something fails during creation (and the caller
	 *         is to blame).
	 */
	public DecisionProcedureGuidance(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, final Signature stopSignature) 
	throws GuidanceException {
		super(component, calc); 
		this.seenObjects = new HashSet<>();
		this.failedConcrete = false;
		this.catastrophicFailure = null;
		this.ended = false;

		//builds the runner actions
		final Actions a = new Actions() {
			@Override
			public boolean atStepPre() {
				try {
				    final State currentState = getEngine().getCurrentState();
					return (currentState.getCurrentMethodSignature().equals(stopSignature));
				} catch (ThreadStackEmptyException e) {
					//this should never happen
					catastrophicFailure = e;
					return true;
				}
			}
			
			@Override
			public boolean atStepPost() {
				updateFailedConcrete();
				return failedConcrete;
			}
			
			@Override
			public boolean atTraceEnd() {
				//trace ended before meeting the stop method
				failedConcrete = true;
				return true;
			}
		};
		runnerParameters.setActions(a);
		
		//builds the private runner
		final Runner runner;
		try {
			final RunnerBuilder b = new RunnerBuilder();
			runner = b.build(runnerParameters);
			this.engine = b.getEngine();
		} catch (CannotBuildEngineException | InitializationException | ClasspathException e) {
			//CannotBuildEngineException may happen if something goes wrong in the construction 
		    //of the decision procedure
			//InitializationException happens when the method does not exist or is native
		    //ClasspathException happens when the classpath does not point to a valid JRE
			throw new GuidanceException(e);
		} catch (NonexistingObservedVariablesException | DecisionException | InvalidClassFileFactoryClassException e) {
			//NonexistingObservedVariablesException should not happen since this decision procedure does not register any variable observer
			//DecisionException should not happen since it happens only when the initial path condition is contradictory
			//InvalidClassFileFactoryClassException should not happen since we use the default class file factory (javassist)
			throw new UnexpectedInternalException(e);
		}
		
		//disables theorem proving (this is concrete execution)
		goFastAndImprecise();

		//runs the private engine until it arrives at methodToRun
		try {
			runner.run();
        } catch (ClasspathException e) {
            throw new GuidanceException(e);
		} catch (CannotBacktrackException | EngineStuckException | CannotManageStateException | 
				ContradictionException | FailureException | DecisionException | 
				ThreadStackEmptyException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
        }
		
		//fails catastrophically if the case
		if (this.catastrophicFailure != null) {
			throw new UnexpectedInternalException(this.catastrophicFailure);
		}
		
		//fails if by some reason it fell into symbolic execution
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		
		//saves the current state and its current frame as the 
		//concrete initial state/frame
		this.initialStateConcrete = this.engine.getCurrentState().clone();
		try {
            this.rootFrameConcrete = this.initialStateConcrete.getCurrentFrame();
        } catch (ThreadStackEmptyException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
		
		//the (resolved) root object is put in seenObject, if present
		Value refToRoot;
        try {
            final ClassHierarchy hier = this.initialStateConcrete.getClassHierarchy();
            final Signature currentMethod = this.initialStateConcrete.getCurrentMethodSignature();
            refToRoot = hier.getClassFile(currentMethod.getClassName()).isMethodStatic(currentMethod) ?
                        null :
                        getValue(this.initialStateConcrete, this.rootFrameConcrete, MemoryPath.mkLocalVariable("this"));
        } catch (GuidanceException | ThreadStackEmptyException | 
                 MethodNotFoundException | BadClassFileException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
		if (refToRoot != null) {
			this.seenObjects.add(Util.heapPosition(this.initialStateConcrete, (Reference) refToRoot));
		}
	}
	
	public void step() throws CannotManageStateException, GuidanceException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		if (this.ended) {
			return;
		}
		if (this.engine.canStep()) {
			try {
				this.engine.step();
			} catch (DecisionException | EngineStuckException | 
					ThreadStackEmptyException e) {
				//should never happen if guidance is correctly implemented
				throw new UnexpectedInternalException(e);
			} catch (ContradictionException | FailureException e) {
				//failed an assumption or an assertion; this ends both the guided 
				//and the guiding execution
				return;
			} catch (ClasspathException e) {
			    throw new GuidanceException(e);
            }
			updateFailedConcrete();
			if (this.failedConcrete) {
				throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
			}
		} else {
			//this happens if guidance and guided do not make the same number of steps
			throw new GuidanceException(ERROR_DIVERGENCE);
		}
	}
	
    /**
     * Returns the {@link Signature} of the  
     * guiding engine's current method.
     * 
     * @return a {@link Signature}.
     * @throws ThreadStackEmptyException if the stack is empty.
     */
	public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
	    return this.engine.getCurrentState().getCurrentMethodSignature();
	}
	
	/**
	 * Ends guidance decision, and falls back on the 
	 * component decision procedure.
	 */
	public void endGuidance() {
        this.ended = true;
        stopFastAndImprecise();
		try {
			this.engine.close();
		} catch (DecisionException e) {
			throw new UnexpectedInternalException(e);
		}
	}
	
	@Override
	protected Outcome decide_IFX_Nonconcrete(ClassHierarchy hier, Primitive condition, SortedSet<DecisionAlternative_IFX> result) 
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decide_IFX_Nonconcrete(hier, condition, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternative_IFX> it = result.iterator();
				final Primitive conditionNot = condition.not();
				while (it.hasNext()) {
					final DecisionAlternative_IFX da = it.next();
					final Primitive conditionToCheck  = (da.value() ? condition : conditionNot);
					final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
					if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
						it.remove();
					}
				}
			} catch (InvalidTypeException e) {
				//this should never happen as arguments have been checked by the caller
				throw new UnexpectedInternalException(e);
			}
		}
		return retVal;
	}

	@Override
	protected Outcome decide_XCMPY_Nonconcrete(ClassHierarchy hier, Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decide_XCMPY_Nonconcrete(hier, val1, val2, result);
		if (!this.ended) {
			try {
				final Primitive comparisonGT = val1.gt(val2);
				final Primitive comparisonEQ = val1.eq(val2);
				final Primitive comparisonLT = val1.lt(val2);
				final Iterator<DecisionAlternative_XCMPY> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternative_XCMPY da = it.next();
					final Primitive conditionToCheck  = 
							(da.operator() == Operator.GT ? comparisonGT :
								da.operator() == Operator.EQ ? comparisonEQ :
									comparisonLT);
					final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
					if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
						it.remove();
					}
				}
			} catch (InvalidTypeException | InvalidOperandException e) {
				//this should never happen as arguments have been checked by the caller
				throw new UnexpectedInternalException(e);
			}
		}
		return retVal;
	}
	
	@Override
	protected Outcome decide_XSWITCH_Nonconcrete(ClassHierarchy hier, Primitive selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decide_XSWITCH_Nonconcrete(hier, selector, tab, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternative_XSWITCH> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternative_XSWITCH da = it.next();
					final Primitive conditionToCheck;
					conditionToCheck = (da.isDefault() ?
					    tab.getDefaultClause(selector) :
					    selector.eq(this.initialStateConcrete.getCalculator().valInt(da.value())));
					final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
					if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
						it.remove();
					}
				}
			} catch (InvalidOperandException | InvalidTypeException e) {
				//this should never happen as arguments have been checked by the caller
				throw new UnexpectedInternalException(e);
			}
		}
		return retVal;
	}
	
	@Override
	protected Outcome decide_XNEWARRAY_Nonconcrete(ClassHierarchy hier, Primitive countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decide_XNEWARRAY_Nonconcrete(hier, countsNonNegative, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternative_XNEWARRAY> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternative_XNEWARRAY da = it.next();
					final Primitive conditionToCheck = (da.ok() ? countsNonNegative : countsNonNegative.not());
					final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
					if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
						it.remove();
					}
				}
			} catch (InvalidTypeException e) {
				//this should never happen as arguments have been checked by the caller
				throw new UnexpectedInternalException(e);
			}
		}
		return retVal;
	}

	@Override
	protected Outcome decide_XASTORE_Nonconcrete(ClassHierarchy hier, Primitive inRange, SortedSet<DecisionAlternative_XASTORE> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decide_XASTORE_Nonconcrete(hier, inRange, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternative_XASTORE> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternative_XASTORE da = it.next();
					final Primitive conditionToCheck = (da.isInRange() ? inRange : inRange.not());
					final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
					if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
						it.remove();
					}
				}
			} catch (InvalidTypeException e) {
				//this should never happen as arguments have been checked by the caller
				throw new UnexpectedInternalException(e);
			}
		}
		return retVal;
	}
	
	@Override
	protected Outcome resolve_XLOAD_GETX_Unresolved(State state, ReferenceSymbolic refToLoad, SortedSet<DecisionAlternative_XLOAD_GETX> result)
	throws DecisionException, BadClassFileException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		updateExpansionBackdoor(state, refToLoad);
		final Outcome retVal = super.resolve_XLOAD_GETX_Unresolved(state, refToLoad, result);
		if (!this.ended) {
			final Iterator<DecisionAlternative_XLOAD_GETX> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternative_XYLOAD_GETX_Unresolved dar = (DecisionAlternative_XYLOAD_GETX_Unresolved) it.next();
				filter(state, refToLoad, dar, it);
			}
		}
		return retVal;
	}
	
	@Override
	protected Outcome resolve_XALOAD_ResolvedNonconcrete(ClassHierarchy hier, Expression accessExpression, Value valueToLoad, boolean fresh, SortedSet<DecisionAlternative_XALOAD> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolve_XALOAD_ResolvedNonconcrete(hier, accessExpression, valueToLoad, fresh, result);
		if (!this.ended) {
			final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternative_XALOAD da = it.next();
				final Primitive conditionToCheck = da.getArrayAccessExpression();
				final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
				if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
					it.remove();
				}
			}
		}
		return retVal;
	}

	@Override
	protected Outcome resolve_XALOAD_Unresolved(State state, Expression accessExpression, ReferenceSymbolic refToLoad, boolean fresh, SortedSet<DecisionAlternative_XALOAD> result)
	throws DecisionException, BadClassFileException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
        updateExpansionBackdoor(state, refToLoad);
		final Outcome retVal = super.resolve_XALOAD_Unresolved(state, accessExpression, refToLoad, fresh, result);
		if (!this.ended) {
			final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternative_XALOAD_Unresolved dar = (DecisionAlternative_XALOAD_Unresolved) it.next();
				final Primitive conditionToCheck = dar.getArrayAccessExpression();
				final Primitive valueInConcreteState = eval(this.initialStateConcrete, this.rootFrameConcrete, conditionToCheck);
				if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
					it.remove();
				} else {
					filter(state, refToLoad, dar, it);
				}
			}
		}
		return retVal;
	}
	
	private void updateExpansionBackdoor(State state, ReferenceSymbolic refToLoad) throws GuidanceException {
	    final String refType = Type.getReferenceClassName(refToLoad.getStaticType());
        final ReferenceConcrete refInConcreteState = (ReferenceConcrete) getValue(this.initialStateConcrete, this.rootFrameConcrete, refToLoad.getOrigin());
        if (refInConcreteState.isNull()) {
            return;
        }
        final Objekt objInConcreteState = this.initialStateConcrete.getObject(refInConcreteState);
	    final String objType = objInConcreteState.getType();
	    if (!refType.equals(objType)) {
	        state.getClassHierarchy().addToExpansionBackdoor(refType, objType);
	    }
	}
	
	private void filter(State state, ReferenceSymbolic refToLoad, DecisionAlternative_XYLOAD_GETX_Unresolved dar, Iterator<?> it) 
	throws GuidanceException {
		final Reference refInConcreteState = (Reference) getValue(this.initialStateConcrete, this.rootFrameConcrete, refToLoad.getOrigin());
		if (dar instanceof DecisionAlternative_XYLOAD_GETX_Null && !Util.isNull(this.initialStateConcrete, refInConcreteState)) {
			it.remove();
		} else if (dar instanceof DecisionAlternative_XYLOAD_GETX_Aliases) {
			final DecisionAlternative_XYLOAD_GETX_Aliases dara = (DecisionAlternative_XYLOAD_GETX_Aliases) dar;
			final MemoryPath aliasOrigin = state.getObject(new ReferenceConcrete(dara.getAliasPosition())).getOrigin();
			final Reference aliasInConcreteState = (Reference) getValue(this.initialStateConcrete, this.rootFrameConcrete, aliasOrigin);
			if (!Util.areAlias(this.initialStateConcrete, refInConcreteState, aliasInConcreteState)) {
				it.remove();
			}
		} else if (dar instanceof DecisionAlternative_XYLOAD_GETX_Expands) {
		    final DecisionAlternative_XYLOAD_GETX_Expands dare = (DecisionAlternative_XYLOAD_GETX_Expands) dar;
		    final long refHeapPosInConcreteState = Util.heapPosition(this.initialStateConcrete, refInConcreteState);
			if (Util.isNull(this.initialStateConcrete, refInConcreteState) || 
			    this.seenObjects.contains(refHeapPosInConcreteState) ||
			    !dare.getClassNameOfTargetObject().equals(this.initialStateConcrete.getObject(refInConcreteState).getType())) {
				it.remove();
			} else {
				this.seenObjects.add(refHeapPosInConcreteState);
			}
		}
	}
	
	private void updateFailedConcrete() {
		this.failedConcrete = this.engine.canBacktrack();
	}
	
	private static Value getValue(State state, Frame rootFrame, MemoryPath origin) 
	throws GuidanceException {
        Value fieldValue = null;
	    Objekt o = null;
	    for (Access a : origin) {
	        if (a instanceof AccessLocalVariable) {
	            final AccessLocalVariable al = (AccessLocalVariable) a;
	            fieldValue = rootFrame.getLocalVariableValue(al.variableName());
	            if (fieldValue == null) {
	                throw new GuidanceException(ERROR_BAD_PATH);
	            }
	        } else if (a instanceof AccessStatic) {
	            final AccessStatic as = (AccessStatic) a;
                fieldValue = null;
	            o = state.getKlass(as.className());
            } else if (a instanceof AccessField) {
                if (o == null) {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
                final AccessField af = (AccessField) a;
                fieldValue = o.getFieldValue(af.fieldName());
            } else if (a instanceof AccessArrayLength) {
                if (! (o instanceof Array)) {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
                fieldValue = ((Array) o).getLength();
            } else if (a instanceof AccessArrayMember) {
                if (! (o instanceof Array)) {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
                final AccessArrayMember aa = (AccessArrayMember) a;
                try {
                    for (AccessOutcome ao : ((Array) o).get(eval(state, rootFrame, aa.index()))) {
                        if (ao instanceof AccessOutcomeIn) {
                            final AccessOutcomeIn aoi = (AccessOutcomeIn) ao;
                            fieldValue = aoi.getValue();
                            break;
                        }
                    }
                } catch (InvalidOperandException | InvalidTypeException e) {
                    throw new GuidanceException(e);
                }
            } else if (a instanceof AccessHashCode) {
                if (o == null) {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
                fieldValue = o.getObjektHashCode();
	        }
            if (fieldValue instanceof Reference) {
                o = state.getObject((Reference) fieldValue);
            } else if (fieldValue != null) {
                o = null;
            }
	    }
        if (fieldValue == null) {
            throw new GuidanceException(ERROR_BAD_PATH);
        }
        return fieldValue;
	}
	
	private static Primitive eval(State state, Frame rootFrame, Primitive toEval) 
	throws GuidanceException {
		final Evaluator evaluator = new Evaluator(state, rootFrame);
		try {
			toEval.accept(evaluator);
		} catch (RuntimeException | GuidanceException e) {
			throw e;
		} catch (Exception e) {
			//should not happen
			throw new UnexpectedInternalException(e);
		}
		return evaluator.value;
	}
	
	private static class Evaluator implements PrimitiveVisitor {
		private final State state;
		private final Frame rootFrame;
		private final Calculator calc;
		Primitive value; //the result
		
		public Evaluator(State state, Frame rootFrame) {
			this.state = state;
			this.rootFrame = rootFrame;
			this.calc = state.getCalculator();
		}
		
		@Override
		public void visitAny(Any x) {
			this.value = x;
		}

		@Override
		public void visitExpression(Expression e) throws Exception {
			if (e.isUnary()) {
				e.getOperand().accept(this);
				final Primitive operandValue = this.value;
				if (operandValue == null) {
					this.value = null;
					return;
				}
				this.value = this.calc.applyUnary(e.getOperator(), operandValue);
			} else {
				e.getFirstOperand().accept(this);
				final Primitive firstOperandValue = this.value;
				if (firstOperandValue == null) {
					this.value = null;
					return;
				}
				e.getSecondOperand().accept(this);
				final Primitive secondOperandValue = this.value;
				if (secondOperandValue == null) {
					this.value = null;
					return;
				}
				this.value = this.calc.applyBinary(firstOperandValue, e.getOperator(), secondOperandValue);
			}
		}

		@Override
		public void visitFunctionApplication(FunctionApplication x) throws Exception {
			final Primitive[] args = x.getArgs();
			final Primitive[] argValues = new Primitive[args.length];
			for (int i = 0; i < args.length; ++i) {
				args[i].accept(this);
				argValues[i] = this.value;
				if (argValues[i] == null) {
					this.value = null;
					return;
				}
			}
			this.value = this.calc.applyFunction(x.getType(), x.getOperator(), argValues);
		}

		@Override
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s) throws GuidanceException {
			final Value fieldValue = getValue(this.state, this.rootFrame, s.getOrigin());
			if (fieldValue instanceof Primitive) {
				this.value = (Primitive) fieldValue;
			} else {
				this.value = null;
			}
		}

		@Override
		public void visitSimplex(Simplex x) {
			this.value = x;
		}

		@Override
		public void visitTerm(Term x) {
			this.value = x;
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
			x.getArg().accept(this);
			this.value = this.calc.narrow(x.getType(), this.value);
		}

		@Override
		public void visitWideningConversion(WideningConversion x) throws Exception {
			x.getArg().accept(this);
			this.value = (x.getType() == this.value.getType() ? this.value : this.calc.widen(x.getType(), this.value));
			//note that the concrete this.value could already be widened
			//because of conversion of actual types to computational types
			//through operand stack, see JVMSpec 2.11.1, tab. 2.3
		}
		
	}
	
    private static final String ERROR_NONCONCRETE_GUIDANCE = "Guided execution fell outside the concrete domain.";
    private static final String ERROR_DIVERGENCE = "Guided execution diverged from guiding execution.";
    private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path.";
}
