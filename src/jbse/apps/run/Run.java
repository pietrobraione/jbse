package jbse.apps.run;

import static jbse.apps.Util.LINE_SEP;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

import jbse.JBSE;
import jbse.algo.exc.CannotInvokeNativeException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.UndefInstructionException;
import jbse.apps.DecisionProcedureDecoratorPrint;
import jbse.apps.DecisionProcedureDecoratorTimer;
import jbse.apps.IO;
import jbse.apps.StateFormatter;
import jbse.apps.StateFormatterGraphviz;
import jbse.apps.StateFormatterText;
import jbse.apps.StateFormatterTrace;
import jbse.apps.Timer;
import jbse.apps.Util;
import jbse.apps.run.RunParameters.DecisionProcedureCreationStrategy;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.InteractionMode;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.apps.run.RunParameters.TextMode;
import jbse.apps.run.RunParameters.TraceTypes;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureAlwSat;
import jbse.dec.DecisionProcedureCVC3;
import jbse.dec.DecisionProcedureEquality;
import jbse.dec.DecisionProcedureLICS;
import jbse.dec.DecisionProcedureSicstus;
import jbse.dec.DecisionProcedureSignAnalysis;
import jbse.dec.DecisionProcedureZ3;
import jbse.dec.exc.DecisionBacktrackException;
import jbse.dec.exc.DecisionEmptyException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Engine;
import jbse.jvm.EngineParameters;
import jbse.jvm.Runner;
import jbse.jvm.RunnerBuilder;
import jbse.jvm.RunnerParameters;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.CannotBuildCalculatorException;
import jbse.jvm.exc.CannotBuildDecisionProcedureException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.State;
import jbse.mem.exc.CannotRefineException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.meta.annotations.ConcretizationCheck;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.Rewriter;
import jbse.rewr.RewriterOperationOnSimplex;
import jbse.rules.LICSRulesRepo;
import jbse.tree.StateTree.BranchPoint;

/**
 * Class implementing an interactive runner for symbolically executing a method.
 * At each step of the symbolic execution {@code Run} prints on a set of
 * output and error streams:
 * 
 * <ul>
 * <li>the identifier of the backtrack point created by the step, whenever such
 * a backtrack point is created;</li>
 * <li>the current execution state in textual form.</li>
 * </ul>
 * 
 * {@code Run} executes a trace until it reaches a stuck state; then, if it
 * can backtrack to some state, it asks the user via console whether it should
 * backtrack. {@code Run} tries to establish a connection with a decision
 * procedure to prune infeasible branches; in the case it fails to connect to a
 * valid decision procedure it queries via console the user whenever a branch
 * point is encountered. A {@code Run} session can be automatically dumped
 * to file for offline inspection.
 * 
 * @author Pietro Braione
 */
public class Run {
	/** The {@link RunParameters} of the symbolic execution. */
	private final RunParameters parameters;

	/** The {@link Runner} used to run the method. */
	private Runner runner = null; //TODO build run object during construction and make this final
	
	/** The {@link Engine} underlying {@code runner}. */
	private Engine engine = null; //TODO build run object during construction and make this final
	
	/** The {@link PrintStream}s for the output. */
	private PrintStream[] out = null;

	/** The {@link PrintStream}s for log information. */
	private PrintStream[] log = null;

	/** The {@link PrintStream}s for errors (critical log information). */
	private PrintStream[] err = null;

	/** The {@link StateFormatter} to output states at branches. */
	private StateFormatter formatterBranches = null;

	/** The {@link StateFormatter} to output states not at branches. */
	private StateFormatter formatterOthers = null;

	/** The {@link Timer} for the decision procedure. */
	private Timer timer = null;

	/** The {@link DecisionProcedureConservativeRepOk}, whenever this decision procedure is chosen. */
	private DecisionProcedureConservativeRepOk consRepOk = null;
	
	/** The {@link DecisionProcedureGuidance}, whenever this method is chosen for stepping the {@link Engine}. */
	private DecisionProcedureGuidance guidance = null;
	
	/** A purely numeric decision procedure for concretization checks. */
	private DecisionProcedureAlgorithms decisionProcedureConcretization = null;
	
	/** The concretization checker. */
	private InitialHeapChecker checker = null;

	/** Counter for the number of analyzed traces that are safe (do not violate assertions). */
	private long tracesSafe = 0;
	
	/** Counter for the number of analyzed traces that are unsafe (violate some assertion). */
	private long tracesUnsafe = 0;

	/** Counter for the number of analyzed traces that are safe and concretizable. */
	private long tracesConcretizableSafe = 0;

	/** Counter for the number of analyzed traces that are unsafe and concretizable. */
	private long tracesConcretizableUnsafe = 0;

	/** Counter for the number of analyzed traces that are out of scope and concretizable. */
	private long tracesConcretizableOutOfScope = 0;

	/** The time spent during the concretization checks. */
	private long elapsedTimeConcretization = 0;

	/**
	 * Constructor.
	 */
	public Run(RunParameters parameters) {
		this.parameters = parameters;
		//TODO this should build! Eliminate numeric error codes!
	}

	/**
	 * Enum that classifies traces based on how they terminate.
	 * 
	 * @author Pietro Braione
	 */
	enum TraceKind {
		/** The trace terminates by returning from the target code. */
		SAFE, 
		/** The trace terminates because it fails some assertion. */
		UNSAFE, 
		/** The trace terminates because it is out of scope. */
		OUT_OF_SCOPE,
		/** The trace terminates because it contradicts some assumption. */
		CONTRADICTORY
	}
	
	/**
	 * Enum listing which counter must be incremented for stats.
	 * 
	 * @author Pietro Braione
	 *
	 */
	private enum CounterKind {
		/** Increment the counter of the safe traces. */
		INC_SAFE,
		/** Increment the counter of the unsafe traces. */
		INC_UNSAFE,
		/** Increment the counter of the out of scope traces. */
		INC_OUT_OF_SCOPE
	}
	
	private class ActionsRun extends Runner.Actions {
		private String scopeExhaustionMessage;
		private TraceKind traceKind;
		private boolean isBranch;
		
		/**
		 * Determines whether the stack size of the current state 
		 * is below the maximum treshold for being printed.
		 * 
		 * @return {@code true} iff it is below the treshold.
		 */
		private boolean stackSizeAcceptable() {
		    final State currentState = Run.this.engine.getCurrentState();
			return (Run.this.parameters.stackDepthShow == 0 || 
					 Run.this.parameters.stackDepthShow > currentState.getStackSize());
		}

		/**
		 * Displays the current state and prompts the user in case
		 * of interactive mode
		 * 
		 * @return {@code true} iff the user told to stop execution.
		 */
		private boolean printAndAsk() {
			if (this.scopeExhaustionMessage == null && this.traceKind != TraceKind.CONTRADICTORY && this.stackSizeAcceptable()) {
				try {
		            final State currentState = Run.this.engine.getCurrentState();
					Run.this.printState(currentState, this.isBranch);
				} catch (UnexpectedInternalException e) {
					IO.println(Run.this.err, ERROR_ENGINE_UNEXPECTED);
					IO.printException(Run.this.err, e);
					return true;
				}
			}
			this.isBranch = false; //safe to do here if you do print last because a state is printed at most once

			// prompts the user for next step in case of interactive mode
			boolean stop = false;
			if (Run.this.parameters.interactionMode == InteractionMode.STEP_BY_STEP) {
				String ans = IO.readln(Run.this.out, PROMPT_SHOULD_STEP);
				if (ans.equals("x")) {
					stop = true;
				}
			}			
			return stop;
		}
		
		@Override
		public boolean atTraceStart() {
			//scope not yet exhausted
			this.scopeExhaustionMessage = null;

			//trace initially assumed to be safe
			this.traceKind = TraceKind.SAFE;
			
			//at the start of a trace we are on a branch
			this.isBranch = true;

			//prints the root/branch
			boolean stop = false;
			if ((Run.this.parameters.stepShowMode == StepShowMode.ALL
					|| Run.this.parameters.stepShowMode == StepShowMode.ROOT_BRANCHES_LEAVES)) {
				stop = printAndAsk();
			}
			
			return stop;
		}
		
		@Override
		public boolean atStepPre() {
			//steps the guidance
			if (Run.this.guidance != null) {
				try {
					Run.this.guidance.step();
				} catch (GuidanceException e) {
					IO.println(Run.this.err, ERROR_GUIDANCE_FAILED);
					IO.printException(Run.this.err, e);
					return true;
				} catch (CannotManageStateException | UnexpectedInternalException e) {
					IO.println(Run.this.err, ERROR_ENGINE_UNEXPECTED);
					IO.printException(Run.this.err, e);
					return true;
				}
			}
			
			return super.atStepPre();
		}
		
		@Override
		public boolean atBranch(BranchPoint bp) {
			this.isBranch = true;
			return super.atBranch(bp);
		}

		@Override
		public boolean atContradictionException(ContradictionException e) {
			this.traceKind = TraceKind.CONTRADICTORY;
			return false;
		}

		@Override
		public boolean atFailureException(FailureException e) {
			this.traceKind = TraceKind.UNSAFE;
			return false;
		}
		
		@Override
		public void atTimeout() {
			IO.println(Run.this.log, WARNING_TIMEOUT);
		}
		
		@Override
		public boolean atScopeExhaustionHeap() {
			this.traceKind = TraceKind.OUT_OF_SCOPE;
			this.scopeExhaustionMessage = WARNING_SCOPE_EXHAUSTED_HEAP;
			return super.atScopeExhaustionHeap();
		}
		
		@Override
		public boolean atScopeExhaustionDepth() {
			this.traceKind = TraceKind.OUT_OF_SCOPE;
			this.scopeExhaustionMessage = WARNING_SCOPE_EXHAUSTED_DEPTH;
			return super.atScopeExhaustionDepth();
		}
		
		@Override
		public boolean atScopeExhaustionCount() {
			this.traceKind = TraceKind.OUT_OF_SCOPE;
			this.scopeExhaustionMessage = WARNING_SCOPE_EXHAUSTED_COUNT;
			return super.atScopeExhaustionCount();
		}

		@Override
		public boolean atStepPost() {
		    //if a resolved reference has not been expanded, prints a warning
		    if (Run.this.parameters.showWarnings && 
		        this.getEngine().someReferenceNotExpanded()) {
		        final State currentState = Run.this.engine.getCurrentState();
		        IO.println(Run.this.log, currentState.getIdentifier() + " "
		            + this.getEngine().getNonExpandedReferencesOrigins()
		            + WARNING_PARTIAL_REFERENCE_RESOLUTION);
		    }

		    //prints the state (all+bytecode and branches)
		    boolean stop = false;
		    if (Run.this.parameters.stepShowMode == StepShowMode.ALL || 
		        (Run.this.parameters.stepShowMode == StepShowMode.ROOT_BRANCHES_LEAVES &&
		        this.isBranch)) {
		        stop = printAndAsk();
		    } 
		    return stop;
		}

		
		@Override
		public boolean atSourceRowPost() {
			//prints/asks for the all+source case
			boolean stop = false;
			if (Run.this.parameters.stepShowMode == StepShowMode.SOURCE) {
				stop = printAndAsk();
			} 
			return stop;
		}
		
		@Override
		public boolean atMethodPost() {
			//prints/asks for the all+method case
			boolean stop = false;
			if (Run.this.parameters.stepShowMode == StepShowMode.METHOD) {
				stop = this.printAndAsk();
			}
			return stop;
		}

		@Override
		public boolean atTraceEnd() {
			try {
                final State currentState = Run.this.engine.getCurrentState();
				//prints the leaf state if the case
				if (Run.this.parameters.stepShowMode == StepShowMode.ALL       //already shown
						|| Run.this.parameters.stepShowMode == StepShowMode.SOURCE //already shown
						|| Run.this.parameters.stepShowMode == StepShowMode.METHOD //already shown
						|| Run.this.parameters.stepShowMode == StepShowMode.NONE   //not to show
						|| (this.traceKind == TraceKind.UNSAFE 
						&& !Run.this.parameters.tracesToShow.contains(TraceTypes.UNSAFE))
						|| (this.traceKind == TraceKind.SAFE 
						&& !Run.this.parameters.tracesToShow.contains(TraceTypes.SAFE))
						|| this.traceKind == TraceKind.CONTRADICTORY
						&& !Run.this.parameters.tracesToShow.contains(TraceTypes.CONTRADICTORY)
						|| this.traceKind == TraceKind.OUT_OF_SCOPE
						&& !Run.this.parameters.tracesToShow.contains(TraceTypes.OUT_OF_SCOPE)) { 
					//does nothing, the leaf state has been already printed 
					//or must not be printed at all
				} else {
					//prints the refined root state for the summaries case
					if (Run.this.parameters.stepShowMode == StepShowMode.SUMMARIES) {
						State initialRefined = Run.this.engine.getInitialState();
						initialRefined.refine(currentState);
						Run.this.printState(initialRefined, true);
						IO.print(out, "\n===\n");
					}
					//prints the leaf (stuck) state
					Run.this.printState(currentState,
							(Run.this.parameters.stepShowMode == StepShowMode.LEAVES || 
							Run.this.parameters.stepShowMode == StepShowMode.SUMMARIES));
				} 

				//displays trace end message and updates stats
				switch (this.traceKind) {
				case SAFE:
					if (Run.this.parameters.showWarnings) {
						IO.println(Run.this.log, currentState.getIdentifier() + MSG_SAFE_TRACE);
					}
					++Run.this.tracesSafe;
					if (Run.this.parameters.doConcretization) {
						checkFinalStateIsConcretizable(CounterKind.INC_SAFE);
					}
					break;
				case UNSAFE:
					if (Run.this.parameters.showWarnings) {
						IO.println(Run.this.log, currentState.getIdentifier() + MSG_UNSAFE_TRACE);
					}
					++Run.this.tracesUnsafe;
					if (Run.this.parameters.doConcretization) {
						checkFinalStateIsConcretizable(CounterKind.INC_UNSAFE);
					}
					break;
				case OUT_OF_SCOPE:
					if (Run.this.parameters.showWarnings) {
						IO.println(Run.this.log, currentState.getIdentifier() + this.scopeExhaustionMessage);
					}
					if (Run.this.parameters.doConcretization) {
						checkFinalStateIsConcretizable(CounterKind.INC_OUT_OF_SCOPE);
					}
					break;
				case CONTRADICTORY:
					if (Run.this.parameters.showWarnings) {
						IO.println(Run.this.log, currentState.getIdentifier() + MSG_CONTRADICTORY_TRACE);
					}
					//no counter
					break;
				}
            } catch (CannotRefineException | UnexpectedInternalException e) { //unexpected
                IO.println(Run.this.err, ERROR_ENGINE_UNEXPECTED);
                IO.printException(Run.this.err, e);
                return true;
			}
			return false;
		}

		@Override
		public boolean atBacktrackPre() {
			boolean stop = false;

			// prompts the user for backtrack in the case of interactive mode
			if (Run.this.parameters.interactionMode == InteractionMode.PROMPT_BACKTRACK) {
				final String ans = IO.readln(Run.this.out, PROMPT_SHOULD_BACKTRACK);
				if (ans.equals("x")) {
					stop = true;
				}
			}

			return stop;
		}

		@Override
		public boolean atDecisionException(DecisionException e) 
		throws DecisionException {
			if (e instanceof DecisionEmptyException) { //TODO ugly! Modify the visitor (or remove DecisionEmptyException and use only ContradictionException)
				if (Run.this.parameters.showWarnings) { 
	                final State currentState = Run.this.engine.getCurrentState();
					IO.println(Run.this.err, currentState.getIdentifier() + WARNING_NO_DECISION_ALTERNATIVES);
				}
				this.traceKind = TraceKind.CONTRADICTORY;
				return false;
			} else {
				IO.println(Run.this.err, ERROR_ENGINE_DECISION_PROCEDURE);
				IO.printException(Run.this.err, e);
				return super.atDecisionException(e);
			}
		}

		@Override
		public boolean atEngineStuckException(EngineStuckException e)
		throws EngineStuckException {
			IO.println(Run.this.err, ERROR_ENGINE_STUCK);
			IO.printException(Run.this.err, e);
			return super.atEngineStuckException(e);
		}

		@Override
		public boolean atCannotManageStateException(CannotManageStateException e)
		throws CannotManageStateException {
			if (e instanceof CannotInvokeNativeException) {
				IO.println(Run.this.err, ERROR_CANNOT_INVOKE_NATIVE);
			} else if (e instanceof UndefInstructionException) {
				IO.println(Run.this.err, ERROR_UNDEF_BYTECODE);
			} else { // e instanceof MetaUnsupportedException or UninterpretedUnsupportedException
				// TODO define an error message
				IO.println(Run.this.err, ERROR_ENGINE_UNEXPECTED);
			}
			IO.printException(Run.this.err, e);
			return super.atCannotManageStateException(e);
		}

        @Override
        public boolean atClasspathException(ClasspathException e)
        throws ClasspathException {
            IO.println(Run.this.err, ERROR_BAD_CLASSPATH);
            IO.printException(Run.this.err, e);
            return super.atClasspathException(e);
        }

		@Override
		public boolean atDecisionBacktrackException(DecisionBacktrackException e)
		throws DecisionBacktrackException {
			IO.println(Run.this.err, ERROR_ENGINE_DECISION_PROCEDURE);
			IO.printException(Run.this.err, e);
			return super.atDecisionBacktrackException(e);
		}

		@Override
		public boolean atCannotBacktrackException(CannotBacktrackException e)
		throws CannotBacktrackException {
			IO.println(Run.this.err, ERROR_ENGINE_UNEXPECTED);
			IO.printException(Run.this.err, e);
			return super.atCannotBacktrackException(e);
		}
		
		private void checkFinalStateIsConcretizable(CounterKind ctr) {
			final long startTime = System.currentTimeMillis();
			final boolean concretizable = Run.this.checker.checkHeap(false);
			final long elapsedTime = System.currentTimeMillis() - startTime;
			Run.this.elapsedTimeConcretization += elapsedTime;
			if (concretizable) {
				if (ctr == CounterKind.INC_OUT_OF_SCOPE) {
					++Run.this.tracesConcretizableOutOfScope;
				} else if (ctr == CounterKind.INC_SAFE) {
					++Run.this.tracesConcretizableSafe;
				} else { //ctr == CounterKind.INC_UNSAFE
					++Run.this.tracesConcretizableUnsafe;
				}
			}
            if (Run.this.parameters.showWarnings) {
                final State currentState = Run.this.engine.getCurrentState();
                IO.println(Run.this.log, currentState.getIdentifier() 
                           + (concretizable ? MSG_CONCRETIZABLE_TRACE : MSG_NOT_CONCRETIZABLE_TRACE));
            }
		}
	}

	/**
	 * Runs the method.
	 * 
	 * @return an {@code int} value representing an error code, 
	 * {@code 0} if everything went ok, {@code 1} if the cause 
	 * of the error was external (inputs), {@code 2} if the cause
	 * of the error was internal (bugs).
	 */
	public int run() {
		// sets up this object
		int retVal = build();
		if (retVal > 0) {
			return retVal;
		}
		
		// runs the method
		IO.println(this.log, MSG_START + this.parameters.getMethodSignature() + " at " + new Date() + ".");
		try {
			this.runner.run();
		} catch (ClasspathException | 
		         DecisionException | CannotManageStateException | 
				 EngineStuckException | CannotBacktrackException e) {
			//already reported
			retVal = 1;
		} catch (ThreadStackEmptyException | 
				 ContradictionException | FailureException | 
				 UnexpectedInternalException e) {
			//this should never happen because Actions does not rethrow these exceptions
			IO.println(this.err, ERROR_ENGINE_UNEXPECTED);
			IO.printException(Run.this.err, e);
			retVal = 2;
		}
		
		// prints statistics
        final long stopTime = System.currentTimeMillis();
        final long startTime = this.runner.getStartTime();
        final long elapsedTime = stopTime - startTime;
		final long tracesContradictory = 
				this.runner.getTracesTotal() 
				- this.tracesSafe 
				- this.tracesUnsafe
				- this.runner.getTracesOutOfScope();
        IO.println(this.log, MSG_END + new Date() + ".");
		IO.println(this.log, 
				MSG_END_STATES + this.engine.getAnalyzedStates() + ", "
				+ MSG_END_TRACES_TOT + this.runner.getTracesTotal() + ", "
				+ MSG_END_TRACES_SAFE + this.tracesSafe 
				+ (Run.this.parameters.doConcretization ? 
						" (" + this.tracesConcretizableSafe + " concretizable)"
					: "")
				+ ", "
				+ MSG_END_TRACES_UNSAFE + this.tracesUnsafe 
				+ (Run.this.parameters.doConcretization ? 
						" (" + this.tracesConcretizableUnsafe + " concretizable)"
					: "")
				+ ", "
				+ MSG_END_TRACES_OUT_OF_SCOPE + this.runner.getTracesOutOfScope()
				+ (Run.this.parameters.doConcretization ? 
						" (" + this.tracesConcretizableOutOfScope + " concretizable)"  
					: "")
				+ ", "
				+ MSG_END_TRACES_VIOLATING_ASSUMPTION + tracesContradictory + ".");
		IO.print(this.log, 
				MSG_END_ELAPSED + Util.formatTime(elapsedTime) + ", "
				+ MSG_END_SPEED + this.engine.getAnalyzedStates() * 1000 / elapsedTime + " states/sec"
				+ (Run.this.parameters.doConcretization ? 
						", " + MSG_END_ELAPSED_CONCRETIZATION + Util.formatTime(elapsedTimeConcretization)
						+ ", (" + Util.formatTimePercent(elapsedTimeConcretization, elapsedTime) + " of total)"
					: ""));
		if (this.timer == null) {
			IO.println(this.log, ".");
		} else {
			final long elapsedTimeDecisionProcedure = this.timer.getTime();
			IO.println(this.log, 
				", " + MSG_END_DECISION + Util.formatTime(elapsedTimeDecisionProcedure) 
				+ " (" + Util.formatTimePercent(elapsedTimeDecisionProcedure, elapsedTime) + " of total).");
		}

		// quits the engine
		try {
			this.engine.close();
		} catch (DecisionException e) {
			IO.println(this.err, ERROR_ENGINE_QUIT_DECISION_PROCEDURE);
			IO.printException(this.err, e);
			retVal = 1;
		} catch (UnexpectedInternalException e) {
			IO.println(this.err, ERROR_ENGINE_UNEXPECTED);
			IO.printException(Run.this.err, e);
			retVal = 2;
		}

		// closes all the output streams with the exception of
		// stdout/err
		for (PrintStream p : this.out) {
			if (p != null && p != System.out) {
				p.close();
			}
		}
		for (PrintStream p : this.err) {
			if (p != null && p != System.err) {
				p.close();
			}
		}

		// returns the error code
		return retVal;
	}
	
	/**
	 * Prints a line of text on the output.
	 * 
	 * @param s the text to be printed.
	 */
	public void out(String s) {
		IO.println(this.out, s);
	}

	/**
	 * Prints a line of text on the log.
	 * 
	 * @param s the text to be printed.
	 */
	public void log(String s) {
		IO.println(this.log, s);
	}

	/**
	 * Prints a line of text on the error.
	 * 
	 * @param s the text to be printed.
	 */
	public void err(String s) {
		IO.println(this.err, s);
	}

	/**
	 * Processes the provided {@link RunParameters} and builds the {@link Engine}
	 * which will be used by the runner to perform the symbolic execution.
	 * 
	 * @return a {@code int} value representing an error code.
	 */
	private int build() {
		//TODO possibly move inside a builder
		//TODO lots of controls on parameters
		//TODO rethrow exception rather than returning an int, and move logging in the receiver

		// sets the output and error streams
		// first are to standard
		this.out = new PrintStream[2];
		this.log = new PrintStream[2];
		this.err = new PrintStream[2];
		this.out[0] = System.out;
		this.log[0] = System.err;
		this.err[0] = System.err;

		// tries to open the dump file
		if (this.parameters.outFileName == null) {
			this.err[1] = null;
		} else {
			try {
				final File f = new File(this.parameters.outFileName);
				this.err[1] = new PrintStream(f);
			} catch (Exception e) {
				IO.println(this.err, ERROR_DUMP_FILE_OPEN);
				this.err[1] = null;
			}
		}
		this.out[1] = this.log[1] = this.err[1];

		// sets line separator style
		if (this.parameters.textMode == TextMode.WINDOWS) {
			System.setProperty("line.separator", "\r\n");
		} else if (this.parameters.textMode == TextMode.UNIX) {
			System.setProperty("line.separator", "\n");
		}

		// prints a welcome message
		IO.println(this.log, MSG_WELCOME_TXT);

		// creates the output formatter
		if (this.parameters.stateFormatMode == StateFormatMode.FULLTEXT) {
			this.formatterBranches = new StateFormatterText(this.parameters.srcPath) {
				@Override
				public void format(State s) {
					this.formatOutput += LINE_SEP; // gutter
					this.formatOutput += banner(s.getIdentifier() + "["
							+ s.getSequenceNumber() + "]", true) ;
					this.formatOutput += LINE_SEP; // gutter
					super.format(s);
				}

				public void emit() {
					IO.print(Run.this.out, this.formatOutput);
				}
			};
			this.formatterOthers = new StateFormatterText(this.parameters.srcPath) {
				@Override
				public void format(State s) {
					this.formatOutput += LINE_SEP; //gutter
					this.formatOutput += banner(s.getIdentifier() + "["
							+ s.getSequenceNumber() + "]", false);
					this.formatOutput += LINE_SEP; //gutter
					super.format(s);
				}

				public void emit() {
					IO.print(Run.this.out, this.formatOutput);
				}
			};
		} else if (this.parameters.stateFormatMode == StateFormatMode.GRAPHVIZ) {
			this.formatterBranches = new StateFormatterGraphviz() {
				public void emit() {
					IO.println(Run.this.out, this.formatOutput);
				}
			};
			this.formatterOthers = new StateFormatterGraphviz() {
				public void emit() {
					IO.println(Run.this.out, this.formatOutput);
				}
			};
		} else { // this.parameters.outputFormatMode == OutputFormatMode.TRACE
			this.formatterBranches = new StateFormatterTrace() {
				public void emit() {
					IO.println(Run.this.out, this.formatOutput);
				}
			};
			this.formatterOthers = new StateFormatterTrace() {
				public void emit() {
					IO.println(Run.this.out, this.formatOutput);
				}
			};
		}

		//prints some feedback on forthcoming decision procedure creation
		if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.SICSTUS) {
			IO.println(this.log, MSG_TRY_SICSTUS
					+ this.parameters.getExternalDecisionProcedurePath() + ".");
		} else if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.CVC3) {
			IO.println(this.log, MSG_TRY_CVC3
					+ this.parameters.getExternalDecisionProcedurePath() + ".");
		} else if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.Z3) {
			IO.println(this.log, MSG_TRY_Z3
					+ this.parameters.getExternalDecisionProcedurePath() + ".");
		} else if (this.parameters.interactionMode == InteractionMode.NO_INTERACTION) {
			IO.println(this.log, MSG_DECISION_BASIC);
		} else {
			IO.println(this.log, MSG_DECISION_INTERACTIVE);
		}
		
		//defines the engine builder
		final RunnerParameters runnerParameters = this.parameters.getRunnerParameters();
		final EngineParameters engineParameters = runnerParameters.getEngineParameters();
		runnerParameters.setActions(new ActionsRun());

		// builds the runner
		try {
			final CalculatorRewriting calc = createCalculator();
			final DecisionProcedureAlgorithms dec = createDecisionProcedure(calc);
			engineParameters.setCalculator(calc);
			engineParameters.setDecisionProcedure(dec);
			final RunnerBuilder rb = new RunnerBuilder();
			this.runner = rb.build(parameters.getRunnerParameters());
			this.engine = rb.getEngine();
			if (this.engine == null) {
				return 1;
			}
			if (this.consRepOk != null) {
				this.consRepOk.setInitialStateSupplier(() -> this.engine.getInitialState()); 
				this.consRepOk.setCurrentStateSupplier(() -> this.engine.getCurrentState()); 
			}
			if (this.parameters.doConcretization) {
			    final RunnerParameters checkerParameters = this.parameters.getConcretizationDriverParameters();
			    checkerParameters.setDecisionProcedure(this.decisionProcedureConcretization);
	            this.checker = 
                    new InitialHeapChecker(checkerParameters, ConcretizationCheck.class, this.parameters.concretizationMethods);
	            this.checker.setInitialStateSupplier(() -> this.engine.getInitialState()); 
                this.checker.setCurrentStateSupplier(() -> this.engine.getCurrentState()); 
			}
		} catch (CannotBuildEngineException e) {
			if (e instanceof CannotBuildDecisionProcedureException) {
				if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.SICSTUS) {
					IO.println(err, ERROR_SICSTUS_FAILED + e.getCause() + ".");
				} else if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.CVC3) {
					IO.println(err, ERROR_CVC3_FAILED + e.getCause() + ".");
				} else {
					IO.println(err, ERROR_Z3_FAILED + e.getCause() + ".");
				}
			} else {
				IO.println(this.err, ERROR_BUILD_FAILED + e.getCause() + ".");
			}
			return 1;
		} catch (DecisionException e) {
			IO.println(this.err, ERROR_ENGINE_INIT_DECISION_PROCEDURE);
			IO.printException(this.err, e);
			return 1;
		} catch (InitializationException e) {
			IO.println(this.err, ERROR_ENGINE_INIT_INITIAL_STATE);
			IO.printException(this.err, e);
			return 1;
		} catch (NonexistingObservedVariablesException e) {
			for (int i : e.getVariableIndices()) {
				if (Run.this.parameters.showWarnings) {
					IO.println(this.log, WARNING_PARAMETERS_UNRECOGNIZABLE_VARIABLE + i
							+ (i == 1 ? "-st." : i == 2 ? "-nd." : i == 3 ? "-rd." : "-th."));
				}
			}
		} catch (InvalidClassFileFactoryClassException e) {
			IO.println(this.err, ERROR_ENGINE_CONFIGURATION);
			IO.printException(Run.this.err, e);
			return 1;
        } catch (ClasspathException e) {
            IO.println(this.err, ERROR_BAD_CLASSPATH);
            IO.printException(Run.this.err, e);
            return 1;
		} catch (UnexpectedInternalException e) {
			IO.println(this.err, ERROR_ENGINE_UNEXPECTED);
			IO.printException(Run.this.err, e);
			return 2;
		}
		
		return 0;
	}
	
	private CalculatorRewriting createCalculator() throws CannotBuildEngineException {
		final CalculatorRewriting calc;
		try {
			calc = new CalculatorRewriting();
			calc.addRewriter(new RewriterOperationOnSimplex()); //indispensable
			for (final Class<? extends Rewriter> rewriterClass : this.parameters.rewriterClasses) {
				if (rewriterClass == null) { 
				    //no rewriter
				    continue; 
				}
				final Rewriter rewriter = (Rewriter) rewriterClass.newInstance();
				calc.addRewriter(rewriter);
			}
		} catch (InstantiationException | IllegalAccessException | UnexpectedInternalException e) {
			throw new CannotBuildCalculatorException(e);
		}
		return calc;
	}
	
	private DecisionProcedureAlgorithms createDecisionProcedure(CalculatorRewriting calc)
	throws CannotBuildDecisionProcedureException {
	    final boolean needHeapCheck = (this.parameters.useConservativeRepOks || this.parameters.doConcretization);
	    
		//initializes cores
		DecisionProcedure core = new DecisionProcedureAlwSat();
		DecisionProcedure coreNumeric = (needHeapCheck ? new DecisionProcedureAlwSat() : null);
		
		//wraps cores with external numeric decision procedure
		final DecisionProcedureType type = this.parameters.getDecisionProcedureType();
		final String path = this.parameters.getExternalDecisionProcedurePath();		
		try {
			if (type == DecisionProcedureType.SICSTUS) {
				core = new DecisionProcedureSicstus(core, calc, path);
				coreNumeric = (needHeapCheck ? new DecisionProcedureSicstus(coreNumeric, calc, path) : null);
			} else if (type == DecisionProcedureType.CVC3) {
				core = new DecisionProcedureCVC3(core, calc, path);
				coreNumeric = (needHeapCheck ? new DecisionProcedureCVC3(coreNumeric, calc, path) : null);
			} else if (type == DecisionProcedureType.Z3) {
				core = new DecisionProcedureZ3(core, calc, path);
				coreNumeric = (needHeapCheck ? new DecisionProcedureZ3(coreNumeric, calc, path) : null);
			} else { //DecisionProcedureType.ALL_SAT
				//do nothing
			}				
		} catch (DecisionException e) {
			throw new CannotBuildDecisionProcedureException(e);
		}
		
		//further wraps cores with sign analysis, if required
		if (this.parameters.doSignAnalysis) {
			core = new DecisionProcedureSignAnalysis(core, calc);
			coreNumeric = (needHeapCheck ? new DecisionProcedureSignAnalysis(coreNumeric, calc) : null);
		}
		
		//further wraps cores with equality analysis, if required
		if (this.parameters.doEqualityAnalysis) {
			core = new DecisionProcedureEquality(core, calc);
			coreNumeric = (needHeapCheck ? new DecisionProcedureEquality(coreNumeric, calc) : null);
		}
		
		//sets the decision procedure for checkers
		if (needHeapCheck) {
		    this.decisionProcedureConcretization = new DecisionProcedureAlgorithms(coreNumeric, calc);
		}
		
		//further wraps core with LICS decision procedure
		if (this.parameters.useLICS) {
			final LICSRulesRepo rulesLICS = this.parameters.getRulesLICS();
			core = new DecisionProcedureLICS(core, calc, rulesLICS);
		}
		
		//further wraps core with conservative repOk decision procedure
		if (this.parameters.useConservativeRepOks) {
		    final RunnerParameters checkerParameters = this.parameters.getConcretizationDriverParameters();
		    checkerParameters.setDecisionProcedure(this.decisionProcedureConcretization);
			this.consRepOk = 
			    new DecisionProcedureConservativeRepOk(core, calc, checkerParameters, this.parameters.conservativeRepOks);
			core = this.consRepOk;
		}

		//wraps core with custom wrappers
		for (DecisionProcedureCreationStrategy c : this.parameters.creationStrategies) {
			core = c.createAndWrap(core, calc);
		}

		//wraps with timer
		final DecisionProcedureDecoratorTimer tCore = new DecisionProcedureDecoratorTimer(core);
		this.timer = tCore;
		core = tCore;

		//wraps with printer if interaction with decision procedure must be shown
		if (this.parameters.showDecisionProcedureInteraction) {
			core = new DecisionProcedureDecoratorPrint(core, out);
		}
				
		//finally guidance
		if (this.parameters.isGuided()) {
			final RunnerParameters guidanceDriverParameters = this.parameters.getGuidanceDriverParameters(calc);
			IO.println(log, MSG_TRY_GUIDANCE + guidanceDriverParameters.getMethodSignature() + ".");
			try {
				this.guidance = new DecisionProcedureGuidance(core, calc, guidanceDriverParameters, this.parameters.getMethodSignature());
			} catch (GuidanceException | UnexpectedInternalException e) {
				IO.println(err, ERROR_GUIDANCE_FAILED + e.getMessage());
				throw new CannotBuildDecisionProcedureException(e);
			}
			core = this.guidance;
		}
		
		//returns the result
		if (core instanceof DecisionProcedureAlgorithms) {
			return (DecisionProcedureAlgorithms) core;
		}
		return new DecisionProcedureAlgorithms(core, calc);
	}	

	/**
	 * Displays a {@link State}.
	 * 
	 * @param s
	 *            the {@link State} to be displayed.
	 * @param isRootBranch
	 *            {@code true} iff {@code s} is at a branch point.
	 */
	private void printState(State s, boolean isRootBranch) {
		final StateFormatter f = 
			(isRootBranch ? this.formatterBranches : this.formatterOthers);
		f.format(s);
		f.emit();
		f.cleanup();
	}

	/**
	 * Returns a banner around a {@link String}.
	 * 
	 * @param s a {@link String} around which the banner is built, or
	 *            {@link null} for a clean banner.
	 * @return the banner.
	 */
	private static String banner(String s, boolean branch) {
		String retVal = (branch ? (".:: " + s + " ::. ") : (s + " "));
		final StringBuilder buf = new StringBuilder();
		for (int i = retVal.length() + 1; i <= BANNER_LENGTH; i++) {
			buf.append(BANNER_CHAR);
		}
		retVal += buf.toString();
		return retVal;
	}

	// Static final members.

	/** Length of separator between text areas. */
	private static final int BANNER_LENGTH = 35;

	/** Char for separator between text areas. */
	private static final char BANNER_CHAR = '.';

	/** Message: welcome. */
	private static final String MSG_WELCOME_TXT = "This is the " + JBSE.NAME + "'s Run Tool (" + JBSE.ACRONYM + " v." + JBSE.VERSION +").";

	/** Message: trying to connect to Sicstus. */
	private static final String MSG_TRY_SICSTUS = "Connecting to Sicstus at ";

	/** Message: trying to connect to CVC3. */
	private static final String MSG_TRY_CVC3 = "Connecting to CVC3 at ";

	/** Message: trying to connect to CVC3. */
	private static final String MSG_TRY_Z3 = "Connecting to Z3 at ";

	/** Message: trying to initialize guidance. */
	private static final String MSG_TRY_GUIDANCE = "Initializing guidance by driver method ";

	/** Message: start of symbolic execution. */
	private static final String MSG_START = "Starting symbolic execution of method ";

	/** Message: the trace is safe. */
	private static final String MSG_SAFE_TRACE = " trace is safe.";

	/** Message: the trace is unsafe (violated an assertion). */
	private static final String MSG_UNSAFE_TRACE = " trace violates an assertion.";

	/** Message: the trace violated an assumption. */
	private static final String MSG_CONTRADICTORY_TRACE = " trace violates an assumption.";

	/** Message: the trace violated an assumption. */
	private static final String MSG_CONCRETIZABLE_TRACE = " trace has a concretizable final state.";

	/** Message: the trace violated an assumption. */
	private static final String MSG_NOT_CONCRETIZABLE_TRACE = " trace has not a concretizable final state.";

	/** Message: end of symbolic execution. */
	private static final String MSG_END = "Symbolic execution finished at ";

	/** Message: elapsed time. */
	private static final String MSG_END_ELAPSED = "Elapsed time: ";

	/** Message: elapsed time. */
	private static final String MSG_END_ELAPSED_CONCRETIZATION = "Elapsed concretization time: ";

	/** Message: elapsed time. */
	private static final String MSG_END_DECISION = "Elapsed time in decision procedure: ";

	/** Message: average speed. */
	private static final String MSG_END_SPEED = "Average speed: ";

	/** Message: analyzed states. */
	private static final String MSG_END_STATES = "Analyzed states: ";

	/** Message: total traces. */
	private static final String MSG_END_TRACES_TOT = "Analyzed traces: ";

	/** Message: total traces violating assumptions. */
	private static final String MSG_END_TRACES_VIOLATING_ASSUMPTION = "Violating assumptions: ";
	
	/** Message: total safe traces. */
	private static final String MSG_END_TRACES_SAFE = "Safe: ";
	
	/** Message: total unsafe traces. */
	private static final String MSG_END_TRACES_UNSAFE = "Unsafe: ";
	
	/** Message: total traces. */
	private static final String MSG_END_TRACES_OUT_OF_SCOPE = "Out of scope: ";

	/** Message: will consider all the clauses satisfiable. */
	private static final String MSG_DECISION_BASIC = "Will use a noninteractive, always-sat decision procedure when necessary.";

	/** Message: will ask to the user whether clauses are satisfiable or not. */
	private static final String MSG_DECISION_INTERACTIVE = "Will query via console about the satisfiability of a clause when necessary.";

	/** Warning: unrecognizable signature. */
	private static final String WARNING_PARAMETERS_UNRECOGNIZABLE_VARIABLE = "Unrecognizable variable will not be observed: ";

	/** Warning: partial reference resolution (part 2). */
	private static final String WARNING_PARTIAL_REFERENCE_RESOLUTION = " not expanded. It may be a " +
			"hint of too strong user-defined constraints, possibly correct when enforcing redundancy by representation invariant.";

	/** Warning: no alternative. */
	private static final String WARNING_NO_DECISION_ALTERNATIVES = " contradictory path condition under any alternative. Will handle " 
		+ "the situation as an undetected assumption violation occurred earlier, but it may be a hint of too strong user-defined constraints.";

	/** Warning: timeout. */
	private static final String WARNING_TIMEOUT = "Timeout.";

	/** Warning: exhausted heap scope. */
	private static final String WARNING_SCOPE_EXHAUSTED_HEAP = " trace exhausted heap scope.";

	/** Warning: exhausted depth scope. */
	private static final String WARNING_SCOPE_EXHAUSTED_DEPTH = " trace exhausted depth scope.";

	/** Warning: exhausted count scope. */
	private static final String WARNING_SCOPE_EXHAUSTED_COUNT = " trace exhausted count scope.";

	/** Error: unrecognizable signature. */
	private static final String ERROR_BUILD_FAILED = "Failed construction of symbolic executor, cause: ";

	/** Error: unable to open dump file. */
	private static final String ERROR_DUMP_FILE_OPEN = "Could not open the dump file. The session will be displayed on console only.";

	/** Error: unable to connect with Sicstus. */
	private static final String ERROR_SICSTUS_FAILED = "Failed connection to Sicstus, cause: ";

	/** Error: unable to connect with CVC3. */
	private static final String ERROR_CVC3_FAILED = "Failed connection to CVC3, cause: ";
	
	/** Error: unable to connect with Z3. */
	private static final String ERROR_Z3_FAILED = "Failed connection to Z3, cause: ";
	
	/** Error: failed guidance. */
	private static final String ERROR_GUIDANCE_FAILED = "Failed guidance, cause: ";

	/**
	 * Error : unable to initialize engine, because unable to initialize
	 * the decision procedure.
	 */
	private static final String ERROR_ENGINE_INIT_DECISION_PROCEDURE = "Unexpected failure during the initialization of the decision procedure.";

	/**
	 * Error: unable to initialize engine, because unable to create the
	 * initial state.
	 */
	private static final String ERROR_ENGINE_INIT_INITIAL_STATE = "Failed initialization of the symbolic execution.";

	/** Error: failure of the decision procedure. */
	private static final String ERROR_ENGINE_DECISION_PROCEDURE = "Unexpected failure of the decision procedude.";

	/**
	 * Error: unable to quit engine, because unable to quit the decision
	 * procedure.
	 */
	private static final String ERROR_ENGINE_QUIT_DECISION_PROCEDURE = "Unexpected failure while quitting the decision procedure.";

	/** Error: cannot manage a bytecode. */
	private static final String ERROR_UNDEF_BYTECODE = "Met an unmanageable bytecode.";

    /** Error: no or bad JRE in the classpath. */
    private static final String ERROR_BAD_CLASSPATH = "No JRE or incompatible JRE in the classpath.";

	/** Error: cannot manage a native method invocation. */
	private static final String ERROR_CANNOT_INVOKE_NATIVE = "Met an unmanageable native method invocation.";

	/** Error: unexpected failure (stepping while engine stuck). */
	private static final String ERROR_ENGINE_STUCK = "Unexpected failure while running (attempted step while in a stuck state).";

	/** Error: failed configuration. */
	private static final String ERROR_ENGINE_CONFIGURATION = "One of the engine's components cannot fit the software architecture.";

	/** Error: unexpected failure. */
	private static final String ERROR_ENGINE_UNEXPECTED = "Unexpected failure.";

	/** Prompt: ask user whether should continue execution by stepping. */
	private static final String PROMPT_SHOULD_STEP = "Proceed with next step? (x: abort, any other: step): ";

	/** Prompt: ask user whether should continue execution by backtracking. */
	private static final String PROMPT_SHOULD_BACKTRACK = "Trace finished, pending backtrack points; proceed with backtrack? (x: abort, any other: backtrack): ";
}