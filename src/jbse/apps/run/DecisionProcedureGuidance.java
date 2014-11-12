package jbse.apps.run;

import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;

import jbse.bc.Signature;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.jvm.CannotBacktrackException;
import jbse.exc.jvm.CannotBuildEngineException;
import jbse.exc.jvm.EngineStuckException;
import jbse.exc.jvm.FailureException;
import jbse.exc.jvm.InitializationException;
import jbse.exc.jvm.NonexistingObservedVariablesException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.Engine;
import jbse.jvm.Runner;
import jbse.jvm.RunnerBuilder;
import jbse.jvm.Runner.Actions;
import jbse.jvm.RunnerParameters;
import jbse.mem.Any;
import jbse.mem.Calculator;
import jbse.mem.Expression;
import jbse.mem.Frame;
import jbse.mem.FunctionApplication;
import jbse.mem.NarrowingConversion;
import jbse.mem.Operator;
import jbse.mem.Primitive;
import jbse.mem.PrimitiveSymbolic;
import jbse.mem.PrimitiveVisitor;
import jbse.mem.Reference;
import jbse.mem.ReferenceConcrete;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.Simplex;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.Term;
import jbse.mem.Util;
import jbse.mem.Value;
import jbse.mem.WideningConversion;
import jbse.tree.DecisionAlternativeAload;
import jbse.tree.DecisionAlternativeAloadRef;
import jbse.tree.DecisionAlternativeAstore;
import jbse.tree.DecisionAlternativeComparison;
import jbse.tree.DecisionAlternativeIf;
import jbse.tree.DecisionAlternativeLFLoad;
import jbse.tree.DecisionAlternativeLoadRef;
import jbse.tree.DecisionAlternativeLoadRefAliases;
import jbse.tree.DecisionAlternativeLoadRefExpands;
import jbse.tree.DecisionAlternativeLoadRefNull;
import jbse.tree.DecisionAlternativeNewarray;
import jbse.tree.DecisionAlternativeSwitch;

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
	 * @throws GuidanceException
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
		} catch (CannotBuildEngineException | InitializationException e) {
			//CannotBuildEngineException may happen if something goes wrong in the construction of the decision procedure
			//InitializationException happens when the method does not exist or is native
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
	protected Outcome decideIfNonconcrete(Primitive condition, SortedSet<DecisionAlternativeIf> result) 
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideIfNonconcrete(condition, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternativeIf> it = result.iterator();
				final Primitive conditionNot = condition.not();
				while (it.hasNext()) {
					final DecisionAlternativeIf da = it.next();
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
	protected Outcome decideComparisonNonconcrete(Primitive val1, Primitive val2, SortedSet<DecisionAlternativeComparison> result)
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
				final Iterator<DecisionAlternativeComparison> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternativeComparison da = it.next();
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
	protected Outcome decideSwitchNonconcrete(Primitive selector, SwitchTable tab, SortedSet<DecisionAlternativeSwitch> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideSwitchNonconcrete(selector, tab, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternativeSwitch> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternativeSwitch da = it.next();
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
	protected Outcome decideNewarrayNonconcrete(Primitive countsNonNegative, SortedSet<DecisionAlternativeNewarray> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideNewarrayNonconcrete(countsNonNegative, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternativeNewarray> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternativeNewarray da = it.next();
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
	protected Outcome decideAstoreNonconcrete(Primitive inRange, SortedSet<DecisionAlternativeAstore> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.decideAstoreNonconcrete(inRange, result);
		if (!this.ended) {
			try {
				final Iterator<DecisionAlternativeAstore> it = result.iterator();
				while (it.hasNext()) {
					final DecisionAlternativeAstore da = it.next();
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
	protected Outcome resolveLFLoadUnresolved(State state, ReferenceSymbolic refToLoad, SortedSet<DecisionAlternativeLFLoad> result)
	throws DecisionException, ClassFileNotFoundException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolveLFLoadUnresolved(state, refToLoad, result);
		if (!this.ended) {
			final Iterator<DecisionAlternativeLFLoad> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternativeLoadRef dar = (DecisionAlternativeLoadRef) it.next();
				filter(state, refToLoad, dar, it);
			}
		}
		return retVal;
	}
	
	@Override
	protected Outcome resolveAloadNonconcrete(Expression accessExpression, Value valueToLoad, boolean fresh, SortedSet<DecisionAlternativeAload> result)
	throws DecisionException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolveAloadNonconcrete(accessExpression, valueToLoad, fresh, result);
		if (!this.ended) {
			final Iterator<DecisionAlternativeAload> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternativeAload da = it.next();
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
	protected Outcome resolveAloadUnresolved(State state, Expression accessExpression, ReferenceSymbolic refToLoad, boolean fresh, SortedSet<DecisionAlternativeAload> result)
	throws DecisionException, ClassFileNotFoundException {
		if (this.failedConcrete) {
			throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
		}
		final Outcome retVal = super.resolveAloadUnresolved(state, accessExpression, refToLoad, fresh, result);
		if (!this.ended) {
			final Iterator<DecisionAlternativeAload> it = result.iterator();
			while (it.hasNext()) {
				final DecisionAlternativeAloadRef dar = (DecisionAlternativeAloadRef) it.next();
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
	
	private void filter(State state, ReferenceSymbolic refToLoad, DecisionAlternativeLoadRef dar, Iterator<?> it) {
		final Reference refInConcreteState = (Reference) getValue(this.initialStateConcrete, this.rootFrameConcrete, refToLoad.getOrigin());
		if (dar instanceof DecisionAlternativeLoadRefNull && !Util.isNull(this.initialStateConcrete, refInConcreteState)) {
			it.remove();
		} else if (dar instanceof DecisionAlternativeLoadRefAliases) {
			final DecisionAlternativeLoadRefAliases dara = (DecisionAlternativeLoadRefAliases) dar;
			final String aliasOrigin = state.getObject(new ReferenceConcrete(dara.getAliasPosition())).getOrigin();
			final Reference aliasInConcreteState = (Reference) getValue(this.initialStateConcrete, this.rootFrameConcrete, aliasOrigin);
			if (!Util.areAlias(this.initialStateConcrete, refInConcreteState, aliasInConcreteState)) {
				it.remove();
			}
		} else if (dar instanceof DecisionAlternativeLoadRefExpands) {
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
