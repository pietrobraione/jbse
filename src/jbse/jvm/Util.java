package jbse.jvm;

import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.PleaseDoNativeException;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NoMethodReceiverException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;

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
	 */
	//TODO handle and convert all these exceptions and raise the abstraction level of the operation
	public static boolean 
	doRunRepOk(State s, Reference r, Signature sig, RunnerParameters p, boolean scopeExhaustionMeansSuccess) 
	throws PleaseDoNativeException, CannotBuildEngineException, InitializationException, InvalidClassFileFactoryClassException, 
	InvalidProgramCounterException, NoMethodReceiverException, InvalidSlotException, 
	NonexistingObservedVariablesException, DecisionException, CannotBacktrackException, 
	CannotManageStateException, ContradictionException, EngineStuckException, FailureException, 
	ClassFileNotFoundException, MethodNotFoundException, IncompatibleClassFileException, 
	ThreadStackEmptyException, OperandStackEmptyException {
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
