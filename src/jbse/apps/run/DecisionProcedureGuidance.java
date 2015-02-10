package jbse.apps.run;

import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
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
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.Util;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Ref;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Ref;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_RefAliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_RefExpands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_RefNull;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
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
 * {@link DecisionProcedure} for guided symbolic execution. It keeps 
 * a parallel
 */
public class DecisionProcedureGuidance extends DecisionProcedureAlgorithms {
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
	 *         is blamed).
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
					return (this.getEngine().getCurrentState().getCurrentMethodSignature().equals(stopSignature));
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
			//CannotBuildEngineException may happen if something goes wrong in the construction of the decision procedure
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
		this.goFastAndImprecise();

		//runs the private engine until it arrives at methodToRun
		try {
			runner.run();
        } catch (ClasspathException e) {
            throw new GuidanceException(e);
		} catch (CannotBacktrackException | EngineStuckException | CannotManageStateException | 
				ContradictionException | FailureException | DecisionException | 
				ThreadStackEmptyException | OperandStackEmptyException e) {
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
		
		//saves the current frame as the "root" frame
		this.initialStateConcrete = this.engine.getCurrentState().clone();
		this.rootFrameConcrete = this.initialStateConcrete.getCurrentFrame();
		
		//the (resolved) root object is put in seenObject, if present
		final Value refToRoot = getValue(this.initialStateConcrete, this.rootFrameConcrete, "{ROOT}:this");
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
					ThreadStackEmptyException | OperandStackEmptyException e) {
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
			//should never happen, guidance and guided should make the same number of steps
			throw new UnexpectedInternalException(); //TODO maybe this is the wrong exception, since it may depend on the caller
		}
	}
	
	/**
	 * Ends guidance decision, and falls back on the component decision procedure.
	 */
	public void endGuidance() {
		this.ended = true;
		this.stopFastAndImprecise();
		try {
			this.engine.close();
		} catch (DecisionException e) {
			throw new UnexpectedInternalException(e);
		}
	}
	
	@Override
	protected Outcome decideIfNonconcrete(Primitive condition, SortedSet<DecisionAlternative_IFX> result) 
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideIfNonconcrete(condition, result);
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
	protected Outcome decideComparisonNonconcrete(Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideComparisonNonconcrete(val1, val2, result);
		if (!this.ended) {
			try {
				final Primitive comparisonGT = val1.gt(val2);
				final Primitive comparisonEQ = val1.eq(val2);
				final Primitive comparisonLT = val1.lt(val2);
				final Iterator<DecisionAlternative_XCMPY> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternative_XCMPY da = it.next();
					final Primitive conditionToCheck  = 
							(da.toOperator() == Operator.GT ? comparisonGT :
								da.toOperator() == Operator.EQ ? comparisonEQ :
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
	protected Outcome decideSwitchNonconcrete(Primitive selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideSwitchNonconcrete(selector, tab, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternative_XSWITCH> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternative_XSWITCH da = it.next();
					final Primitive conditionToCheck;
					conditionToCheck = selector.eq(this.initialStateConcrete.getCalculator().valInt(da.value()));
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
	protected Outcome decideNewarrayNonconcrete(Primitive countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideNewarrayNonconcrete(countsNonNegative, result);
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
	protected Outcome decideAstoreNonconcrete(Primitive inRange, SortedSet<DecisionAlternative_XASTORE> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideAstoreNonconcrete(inRange, result);
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
	protected Outcome resolveLFLoadUnresolved(State state, ReferenceSymbolic refToLoad, SortedSet<DecisionAlternative_XLOAD_GETX> result)
	throws DecisionException, ClassFileNotFoundException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolveLFLoadUnresolved(state, refToLoad, result);
		if (!this.ended) {
			final Iterator<DecisionAlternative_XLOAD_GETX> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternative_XYLOAD_GETX_Ref dar = (DecisionAlternative_XYLOAD_GETX_Ref) it.next();
				filter(state, refToLoad, dar, it);
			}
		}
		return retVal;
	}
	
	@Override
	protected Outcome resolveAloadNonconcrete(Expression accessExpression, Value valueToLoad, boolean fresh, SortedSet<DecisionAlternative_XALOAD> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolveAloadNonconcrete(accessExpression, valueToLoad, fresh, result);
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
	protected Outcome resolveAloadUnresolved(State state, Expression accessExpression, ReferenceSymbolic refToLoad, boolean fresh, SortedSet<DecisionAlternative_XALOAD> result)
	throws DecisionException, ClassFileNotFoundException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolveAloadUnresolved(state, accessExpression, refToLoad, fresh, result);
		if (!this.ended) {
			final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternative_XALOAD_Ref dar = (DecisionAlternative_XALOAD_Ref) it.next();
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
	
	private void filter(State state, ReferenceSymbolic refToLoad, DecisionAlternative_XYLOAD_GETX_Ref dar, Iterator<?> it) {
		final Reference refInConcreteState = (Reference) getValue(this.initialStateConcrete, this.rootFrameConcrete, refToLoad.getOrigin());
		if (dar instanceof DecisionAlternative_XYLOAD_GETX_RefNull && !Util.isNull(this.initialStateConcrete, refInConcreteState)) {
			it.remove();
		} else if (dar instanceof DecisionAlternative_XYLOAD_GETX_RefAliases) {
			final DecisionAlternative_XYLOAD_GETX_RefAliases dara = (DecisionAlternative_XYLOAD_GETX_RefAliases) dar;
			final String aliasOrigin = state.getObject(new ReferenceConcrete(dara.getAliasPosition())).getOrigin();
			final Reference aliasInConcreteState = (Reference) getValue(this.initialStateConcrete, this.rootFrameConcrete, aliasOrigin);
			if (!Util.areAlias(this.initialStateConcrete, refInConcreteState, aliasInConcreteState)) {
				it.remove();
			}
		} else if (dar instanceof DecisionAlternative_XYLOAD_GETX_RefExpands) {
			final long refHeapPosInConcreteState = Util.heapPosition(this.initialStateConcrete, refInConcreteState);
			if (this.seenObjects.contains(refHeapPosInConcreteState)) {
				it.remove();
			} else {
				this.seenObjects.add(refHeapPosInConcreteState);
			}
		}
	}
	
	private void updateFailedConcrete() {
		this.failedConcrete = this.engine.canBacktrack();
	}
	
	private static Value getValue(State state, Frame rootFrame, String origin) {
		final String[] originFields = origin.split("\\.");
		final String rootVariableName = originFields[0].replaceAll("\\{ROOT\\}:", ""); //from {ROOT}:var extracts var
		Value fieldValue = rootFrame.getLocalVariableValue(rootVariableName);
		for (int i = 1; i < originFields.length; ++i) {
			if (fieldValue == null || !(fieldValue instanceof Reference)) {
				return null;
			} else {
				final Reference nextField = (Reference) fieldValue;
				fieldValue = state.getObject(nextField).getFieldValue(originFields[i]);
			}
		}
		return fieldValue;
	}
	
	private static Primitive eval(State state, Frame rootFrame, Primitive toEval) {
		final Evaluator evaluator = new Evaluator(state, rootFrame);
		try {
			toEval.accept(evaluator);
		} catch (RuntimeException e) {
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
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
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
			this.value = this.calc.widen(x.getType(), this.value);
		}
		
	}
	
	private static final String ERROR_NONCONCRETE_GUIDANCE = "Guided execution fell outside the concrete domain; Please improve your driver.";
}
