package jbse.jvm;

import jbse.bc.Signature;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.algo.PleaseDoNativeException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.bc.NoMethodReceiverException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.jvm.CannotBacktrackException;
import jbse.exc.jvm.CannotBuildEngineException;
import jbse.exc.jvm.EngineStuckException;
import jbse.exc.jvm.FailureException;
import jbse.exc.jvm.InitializationException;
import jbse.exc.jvm.NonexistingObservedVariablesException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Reference;
import jbse.mem.Simplex;
import jbse.mem.State;
import jbse.mem.Value;

public final class Util {
	/**
	 * Runs a repOk method in a new {@link Engine}.
	 * 
	 * @param s the initial {@link State} to run the
	 *        repOk method. It will be modified.
	 * @param r a {@link Reference} to the target
	 *        of the method invocation ("this").
	 * @param sig the {@link Signature} of the method to run.
	 * @param p the {@link RunnerParameters} object that will be used to build 
	 *        the runner; It must be coherent with the parameters of the engine
	 *        that created {@code s}. It will be modified.
	 * @param scopeExhaustionMeansSuccess {@code true} iff a trace that exhausts
	 *        the execution scope must be interpreted as a successful 
	 *        execution of the method that returns {@code true}. 
	 * @return {@code true} iff there is at least one successful execution
	 *         of the method that returns {@code true}. 
	 * @throws PleaseDoNativeException
	 * @throws CannotBuildEngineException
	 * @throws DecisionException
	 * @throws InitializationException
	 * @throws InvalidClassFileFactoryClassException
	 * @throws NonexistingObservedVariablesException
	 * @throws CannotBacktrackException
	 * @throws CannotManageStateException
	 * @throws ContradictionException
	 * @throws EngineStuckException
	 * @throws FailureException
	 * @throws InvalidSlotException 
	 * @throws NoMethodReceiverException 
	 * @throws InvalidProgramCounterException 
	 * @throws ThreadStackEmptyException 
	 * @throws IncompatibleClassFileException 
	 * @throws MethodNotFoundException 
	 * @throws ClassFileNotFoundException 
	 * @throws OperandStackEmptyException 
	 * @throws UnexpectedInternalException
	 */
	//TODO handle and convert all these exceptions and raise the abstraction level of the operation
	public static boolean 
	doRunRepOk(State s, Reference r, Signature sig, RunnerParameters p, boolean scopeExhaustionMeansSuccess) 
	throws UnexpectedInternalException, PleaseDoNativeException, CannotBuildEngineException, 
	DecisionException, InitializationException, InvalidClassFileFactoryClassException, NonexistingObservedVariablesException, 
	CannotBacktrackException, CannotManageStateException, ContradictionException, EngineStuckException, FailureException, 
	ClassFileNotFoundException, MethodNotFoundException, IncompatibleClassFileException, ThreadStackEmptyException, 
	InvalidProgramCounterException, NoMethodReceiverException, InvalidSlotException, OperandStackEmptyException {
		//TODO check that sig is the signature of a nonstatic, nonspecial method
		s.pushFrame(sig, true, false, false, 0, r);
		p.setInitialState(s);
		final RepOkRunnerActions actions = new RepOkRunnerActions(scopeExhaustionMeansSuccess);
		p.setActions(actions);

		//runs
		final RunnerBuilder builder = new RunnerBuilder();
		final Runner runner = builder.build(p);
		runner.run();
		return actions.repOk;
	}

	private static class RepOkRunnerActions extends Runner.Actions {
		final boolean scopeExhaustionMeansSuccess;
		boolean repOk = false;
		
		public RepOkRunnerActions(boolean scopeExhaustionMeansSuccess) { 
			this.scopeExhaustionMeansSuccess = scopeExhaustionMeansSuccess;
		}
		
		//TODO log differently!

/*		@Override
		public boolean atStepPost() {
			//final StateFormatterTrace f = new StateFormatterTrace(new ArrayList<String>()) {
			final StateFormatterTrace f = new StateFormatterTrace() {
				@Override
				public void emit() {
					System.out.println("==> " + this.formatOutput);
				}
			};
			f.format(engine.getCurrentState());
			f.emit();
			return super.atStepPost();
		}
*/
		@Override
		public boolean atTraceEnd() {
			final Value retVal = this.getEngine().getCurrentState().getStuckReturn();
			if (retVal != null) {
				final Simplex retValSimplex = (Simplex) retVal;
				this.repOk = (((Integer) retValSimplex.getActualValue()) == 1);
			}
			return repOk; //interrupts symbolic execution if exists a successful trace that returns true
		}
		
		@Override
		public boolean atContradictionException(ContradictionException e)
		throws ContradictionException {
			return false; //assumption violated: move to next trace
		}
		
		@Override
		public boolean atScopeExhaustionHeap() {
			if (scopeExhaustionMeansSuccess) {
				this.repOk = true;
				return true;
			}
			return super.atScopeExhaustionHeap();
			//was: throw new ...whateverException("A conservative repOk must not expand the heap");
		}
		
		@Override
		public boolean atScopeExhaustionCount() {
			if (scopeExhaustionMeansSuccess) {
				this.repOk = true;
				return true;
			}
			return super.atScopeExhaustionCount();
		}
		
		@Override
		public boolean atScopeExhaustionDepth() {
			if (scopeExhaustionMeansSuccess) {
				this.repOk = true;
				return true;
			}
			return super.atScopeExhaustionDepth();
		}
	}

	
	//do not instantiate it!
	private Util() { }
}
