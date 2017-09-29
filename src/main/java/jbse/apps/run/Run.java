package jbse.apps.run;

import static jbse.apps.Util.LINE_SEP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import jbse.JBSE;
import jbse.algo.exc.CannotInvokeNativeException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.algo.exc.UninterpretedUnsupportedException;
import jbse.apps.DecisionProcedureDecoratorPrint;
import jbse.apps.DecisionProcedureDecoratorTimer;
import jbse.apps.IO;
import jbse.apps.Formatter;
import jbse.apps.StateFormatterGraphviz;
import jbse.apps.StateFormatterJUnitTestSuite;
import jbse.apps.StateFormatterText;
import jbse.apps.StateFormatterTrace;
import jbse.apps.Timer;
import jbse.apps.Util;
import jbse.apps.run.RunParameters.DecisionProcedureCreationStrategy;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.GuidanceType;
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
import jbse.dec.DecisionProcedureClassInit;
import jbse.dec.DecisionProcedureEquality;
import jbse.dec.DecisionProcedureLICS;
import jbse.dec.DecisionProcedureSignAnalysis;
import jbse.dec.DecisionProcedureSMTLIB2_AUFNIRA;
import jbse.dec.exc.DecisionBacktrackException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Engine;
import jbse.jvm.EngineParameters;
import jbse.jvm.Runner;
import jbse.jvm.RunnerBuilder;
import jbse.jvm.RunnerParameters;
import jbse.jvm.exc.CannotBacktrackException;
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
import jbse.tree.StateTree.BranchPoint;
import jbse.val.PrimitiveSymbolic;
import jbse.val.Simplex;

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
public final class Run {
	/** The {@link RunParameters} of the symbolic execution. */
	private final RunParameters parameters;

	/** The {@link Runner} used to run the method. */
	private Runner runner = null; //TODO build run object during construction and make this final
	
	/** The {@link Engine} underlying {@code runner}. */
	private Engine engine = null; //TODO build run object during construction and make this final
    
    /** The {@link DecisionProcedure} used by {@code engine}. */
    private DecisionProcedureAlgorithms decisionProcedure = null; //TODO build run object during construction and make this final
	
	/** The {@link PrintStream}s for the output. */
	private PrintStream[] out = null;

	/** The {@link PrintStream}s for log information. */
	private PrintStream[] log = null;

	/** The {@link PrintStream}s for errors (critical log information). */
	private PrintStream[] err = null;

	/** The {@link Formatter} to output states at branches. */
	private Formatter formatterBranches = null;

	/** The {@link Formatter} to output states not at branches. */
	private Formatter formatterOthers = null;

	/** The {@link Timer} for the decision procedure. */
	private Timer timer = null;

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

	/** 
	 * Counter for the number of analyzed traces that are unmanageable 
	 * (the symbolic executor is not able to execute them). 
	 */
	private long tracesUnmanageable = 0;

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
		private String endOfTraceMessage;
		private TraceTypes traceKind;
		private boolean isBranch;
		
		/**
		 * Determines whether the stack size of the current state 
		 * is below the maximum treshold for being printed.
		 * 
		 * @return {@code true} iff it is below the treshold.
		 */
		private boolean stackSizeAcceptable() {
		    final State currentState = Run.this.engine.getCurrentState();
			return (Run.this.parameters.getStackDepthShow() == 0 || 
					 Run.this.parameters.getStackDepthShow() > currentState.getStackSize());
		}

		/**
		 * Displays the current state and prompts the user in case
		 * of interactive mode
		 * 
		 * @return {@code true} iff the user told to stop execution.
		 */
		private boolean printAndAsk() {
			if (this.endOfTraceMessage == null && this.traceKind != TraceTypes.CONTRADICTORY && this.stackSizeAcceptable()) {
				try {
		            final State currentState = Run.this.getCurrentState();
					Run.this.emitState(currentState, this.isBranch);
				} catch (UnexpectedInternalException e) {
				    Run.this.err(ERROR_UNEXPECTED);
				    Run.this.err(e);
					return true;
				}
			}
			this.isBranch = false; //safe to do here if you do print last because a state is printed at most once

			// prompts the user for next step in case of interactive mode
			boolean stop = false;
			if (Run.this.parameters.getInteractionMode() == InteractionMode.STEP_BY_STEP) {
				final String ans = Run.this.in(PROMPT_SHOULD_STEP);
				if (ans.equals("x")) {
					stop = true;
				}
			}			
			return stop;
		}
		
		@Override
		public boolean atRoot() {
		    Run.this.emitPrologue();
		    
            //prints the state (all+bytecode and branches)
            boolean stop = false;
            if (Run.this.parameters.getStepShowMode() == StepShowMode.METHOD || 
                Run.this.parameters.getStepShowMode() == StepShowMode.SOURCE) {
                stop = printAndAsk();
            } 
            return stop;
		}
		
		@Override
		public boolean atTraceStart() {
			//scope not yet exhausted
			this.endOfTraceMessage = null;

			//trace initially assumed to be safe
			this.traceKind = TraceTypes.SAFE;
			
			//at the start of a trace we are on a branch
			this.isBranch = true;

			//exits if user wants
			boolean stop = false;
			if (Run.this.parameters.getStepShowMode() == StepShowMode.ALL ||
				Run.this.parameters.getStepShowMode() == StepShowMode.ROOT_BRANCHES_LEAVES) {
				stop = printAndAsk();
			}
			
			return stop;
		}
		
		@Override
		public boolean atBranch(BranchPoint bp) {
			this.isBranch = true;
			return super.atBranch(bp);
		}
		
		@Override
		public void atEnd() {
		    Run.this.emitEpilogue();
		    super.atEnd();
		}
		
		@Override
		public boolean atContradictionException(ContradictionException e) {
			this.traceKind = TraceTypes.CONTRADICTORY;
			return false;
		}

		@Override
		public boolean atFailureException(FailureException e) {
			this.traceKind = TraceTypes.UNSAFE;
			return false;
		}
		
		@Override
		public void atTimeout() {
            if (Run.this.parameters.getShowWarnings()) {
                Run.this.log(WARNING_TIMEOUT);
            }
		}
		
		@Override
		public boolean atScopeExhaustionHeap() {
			this.traceKind = TraceTypes.OUT_OF_SCOPE;
			this.endOfTraceMessage = WARNING_SCOPE_EXHAUSTED_HEAP;
			return super.atScopeExhaustionHeap();
		}
		
		@Override
		public boolean atScopeExhaustionDepth() {
			this.traceKind = TraceTypes.OUT_OF_SCOPE;
			this.endOfTraceMessage = WARNING_SCOPE_EXHAUSTED_DEPTH;
			return super.atScopeExhaustionDepth();
		}
		
		@Override
		public boolean atScopeExhaustionCount() {
			this.traceKind = TraceTypes.OUT_OF_SCOPE;
			this.endOfTraceMessage = WARNING_SCOPE_EXHAUSTED_COUNT;
			return super.atScopeExhaustionCount();
		}

		@Override
		public boolean atCannotManageStateException(CannotManageStateException e)
		throws CannotManageStateException {
			if (e instanceof CannotInvokeNativeException) {
			    this.traceKind = TraceTypes.UNMANAGEABLE;
				this.endOfTraceMessage = WARNING_CANNOT_INVOKE_NATIVE;
			    return false;
			} else if (e instanceof NotYetImplementedException) {
			    this.traceKind = TraceTypes.UNMANAGEABLE;
			    this.endOfTraceMessage = WARNING_NOT_IMPLEMENTED_BYTECODE;
			    return false;
			} else if (e instanceof MetaUnsupportedException) {
			    this.traceKind = TraceTypes.UNMANAGEABLE;
			    this.endOfTraceMessage = WARNING_META_UNSUPPORTED + e.getMessage();
			    return false;
			} else if (e instanceof UninterpretedUnsupportedException) {
			    this.traceKind = TraceTypes.UNMANAGEABLE;
			    this.endOfTraceMessage = WARNING_UNINTERPRETED_UNSUPPORTED + e.getMessage();
			    return false;
			} else {
			    Run.this.err(ERROR_UNEXPECTED);
				Run.this.err(e);
				return true;
			}
		}

		@Override
		public boolean atStepPost() {
		    //if a resolved reference has not been expanded, prints a warning
		    if (Run.this.parameters.getShowWarnings() && 
		        getEngine().someReferenceNotExpanded()) {
		        final State currentState = Run.this.engine.getCurrentState();
		        Run.this.log(currentState.getIdentifier() + " "
		            + getEngine().getNonExpandedReferencesOrigins()
		            + WARNING_PARTIAL_REFERENCE_RESOLUTION);
		    }

		    //prints/asks (all+bytecode and branches)
		    boolean stop = false;
		    if (Run.this.parameters.getStepShowMode() == StepShowMode.ALL || 
		        (Run.this.parameters.getStepShowMode() == StepShowMode.ROOT_BRANCHES_LEAVES &&
		        this.isBranch)) {
		        stop = printAndAsk();
		    } 
		    return stop;
		}

		
		@Override
		public boolean atSourceRowPost() {
			//prints/asks (all+source)
			boolean stop = false;
			if (Run.this.parameters.getStepShowMode() == StepShowMode.SOURCE) {
				stop = printAndAsk();
			} 
			return stop;
		}
		
		@Override
		public boolean atMethodPost() {
			//prints/asks (all+method)
			boolean stop = false;
			if (Run.this.parameters.getStepShowMode() == StepShowMode.METHOD) {
				stop = this.printAndAsk();
			}
			return stop;
		}

		@Override
		public boolean atTraceEnd() {
			try {
                final State currentState = Run.this.engine.getCurrentState();
				//prints the leaf state if the case
				if (Run.this.parameters.getStepShowMode() == StepShowMode.ALL ||    //already shown
				    Run.this.parameters.getStepShowMode() == StepShowMode.SOURCE || //already shown
				    Run.this.parameters.getStepShowMode() == StepShowMode.METHOD || //already shown
				    Run.this.parameters.getStepShowMode() == StepShowMode.NONE   || //not to show
				    !Run.this.parameters.getTracesToShow().contains(this.traceKind)) {   //not to show
					//does nothing, the leaf state has been already printed 
					//or must not be printed at all
				} else {
					//prints the refined root state for the summaries case
					if (Run.this.parameters.getStepShowMode() == StepShowMode.SUMMARIES) {
						State initialRefined = Run.this.engine.getInitialState();
						initialRefined.refine(currentState);
						Run.this.emitState(initialRefined, true);
						Run.this.out("\n===\n");
					}
					//prints the leaf (stuck) state
					Run.this.emitState(currentState,
							(Run.this.parameters.getStepShowMode() == StepShowMode.LEAVES || 
							Run.this.parameters.getStepShowMode() == StepShowMode.SUMMARIES));
				} 

				//displays trace end message and updates stats
				final CounterKind counterKind;
				switch (this.traceKind) {
				case SAFE:
					++Run.this.tracesSafe;
					this.endOfTraceMessage = MSG_SAFE_TRACE;
					counterKind = CounterKind.INC_SAFE;
					break;
				case UNSAFE:
					++Run.this.tracesUnsafe;
					this.endOfTraceMessage = MSG_UNSAFE_TRACE;
					counterKind = CounterKind.INC_UNSAFE;
					break;
				case OUT_OF_SCOPE:
					//counter is provided by runner
					//this.endOfTraceMessage already set
					counterKind = CounterKind.INC_OUT_OF_SCOPE;
					break;
				case UNMANAGEABLE:
					++Run.this.tracesUnmanageable;
					//this.endOfTraceMessage already set
					counterKind = null;
					break;
				case CONTRADICTORY:
					this.endOfTraceMessage = MSG_CONTRADICTORY_TRACE;
					counterKind = null;
					break;
				default: //to keep compiler happy:
					throw new AssertionError();
				}
                if (Run.this.parameters.getShowWarnings()) {
                    Run.this.log(currentState.getIdentifier() + this.endOfTraceMessage);
                }
                if (Run.this.parameters.getDoConcretization()) {
                    checkFinalStateIsConcretizable(counterKind);
                }
				
            } catch (CannotRefineException e) {
                throw new UnexpectedInternalException(e);
			}
			return false;
		}

		@Override
		public boolean atBacktrackPre() {
			// prompts the user for backtrack in the case of interactive mode
            boolean stop = false;
			if (Run.this.parameters.getInteractionMode() == InteractionMode.PROMPT_BACKTRACK) {
				final String ans = Run.this.in(PROMPT_SHOULD_BACKTRACK);
				if (ans.equals("x")) {
					stop = true;
				}
			}

			return stop;
		}

		@Override
		public boolean atDecisionException(DecisionException e) 
		throws DecisionException {
		    Run.this.err(ERROR_ENGINE_DECISION_PROCEDURE);
		    Run.this.err(e);
		    return super.atDecisionException(e);
		}

		@Override
		public boolean atEngineStuckException(EngineStuckException e)
		throws EngineStuckException {
		    Run.this.err(ERROR_ENGINE_STUCK);
		    Run.this.err(e);
			return super.atEngineStuckException(e);
		}

        @Override
        public boolean atClasspathException(ClasspathException e)
        throws ClasspathException {
            Run.this.err(ERROR_BAD_CLASSPATH);
            Run.this.err(e);
            return super.atClasspathException(e);
        }

		@Override
		public boolean atDecisionBacktrackException(DecisionBacktrackException e)
		throws DecisionBacktrackException {
		    Run.this.err(ERROR_ENGINE_DECISION_PROCEDURE);
		    Run.this.err(e);
			return super.atDecisionBacktrackException(e);
		}

		@Override
		public boolean atCannotBacktrackException(CannotBacktrackException e)
		throws CannotBacktrackException {
		    Run.this.err(ERROR_UNEXPECTED);
		    Run.this.err(e);
			return super.atCannotBacktrackException(e);
		}
		
		private void checkFinalStateIsConcretizable(CounterKind ctr) {
		    if (ctr == null) {
		        return;
		    }
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
            if (Run.this.parameters.getShowWarnings()) {
                final State currentState = Run.this.engine.getCurrentState();
                Run.this.log(currentState.getIdentifier() 
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

		// prints feedback
        if (this.parameters.getShowInfo()) {
            log(MSG_START + this.parameters.getMethodSignature() + " at " + new Date() + ".");
        }
        
        // runs
		try {
			this.runner.run();
		} catch (ClasspathException | 
		         DecisionException | CannotManageStateException | 
				 EngineStuckException | CannotBacktrackException e) {
			//already reported
			retVal = 1;
		} catch (ThreadStackEmptyException | ContradictionException |
		         FailureException | UnexpectedInternalException e) {
			//this should never happen because Actions does not rethrow these exceptions
			err(ERROR_UNEXPECTED);
			err(e);
			retVal = 2;
		}
		
		// prints statistics
        if (this.parameters.getShowInfo()) {
            log(MSG_END + new Date() + ".");
            printFinalStats();
        }

		// closes and returns the error code
		return close();
	}
	
    /**
     * Gets a line of text on the input stream.
     * 
     * @param s the input prompt.
     * @return the read line.
     */
    public String in(String prompt) {
        return IO.readln(this.out, prompt);
    }

	/**
	 * Prints a line of text on the output streams.
	 * 
	 * @param s the text to be printed.
	 */
	public void out(String s) {
		IO.println(this.out, s);
	}

    /**
     * Prints some text on the output streams.
     * 
     * @param s the text to be printed.
     */
    public void outNoBreak(String s) {
        IO.print(this.out, s);
    }

	/**
	 * Prints a line of text on the log streams.
	 * 
	 * @param s the text to be printed.
	 */
	public void log(String s) {
		IO.println(this.log, s);
	}

	/**
	 * Prints a line of text on the error streams.
	 * 
	 * @param s the text to be printed.
	 */
	public void err(String s) {
		IO.println(this.err, s);
	}
	
	/**
	 * Prints a throwable on the error streams.
	 * 
	 * @param t the {@link Throwable} to be printed.
	 */
	public void err(Throwable t) {
	    IO.printException(this.err, t);
	}

    private static final String COMMANDLINE_LAUNCH_Z3   = System.getProperty("os.name").toLowerCase().contains("windows") ? " /smt2 /in /t:10" : " -smt2 -in -t:10";
    private static final String COMMANDLINE_LAUNCH_CVC4 = " --lang=smt2 --output-lang=smt2 --no-interactive --incremental --tlimit-per=10000";
    
	/**
	 * Processes the provided {@link RunParameters} and builds the {@link Engine}
	 * which will be used by the runner to perform the symbolic execution.
	 * 
	 * @return an {@code int} value representing an error code.
	 */
	private int build() {
		//TODO possibly move inside a builder
		//TODO lots of controls on parameters
		//TODO rethrow exception rather than returning an int, and centralize logging in the receiver

	    //sets the input, output and error streams
	    setStreams();
	    
		// prints a welcome message
        if (this.parameters.getShowInfo()) {
            log(MSG_WELCOME_TXT);
        }

		//builds
		try {
			createFormatter();
	        final RunnerParameters runnerParameters = this.parameters.getRunnerParameters();
	        runnerParameters.setActions(new ActionsRun());
            final CalculatorRewriting calc = createCalculator();
	        final EngineParameters engineParameters = runnerParameters.getEngineParameters();
			engineParameters.setCalculator(calc);
            createDecisionProcedure(calc);
			engineParameters.setDecisionProcedure(this.decisionProcedure);
			final RunnerBuilder rb = new RunnerBuilder();
			this.runner = rb.build(this.parameters.getRunnerParameters());
			this.engine = rb.getEngine();
			if (this.engine == null) {
				return 1;
			}
            createHeapChecker(this.decisionProcedureConcretization);
        } catch (NonexistingObservedVariablesException e) {
            for (int i : e.getVariableIndices()) {
                if (Run.this.parameters.getShowWarnings()) {
                    log(WARNING_PARAMETERS_UNRECOGNIZABLE_VARIABLE + i
                        + (i == 1 ? "-st." : i == 2 ? "-nd." : i == 3 ? "-rd." : "-th."));
                }
            }
		} catch (DecisionException e) {
			err(ERROR_ENGINE_INIT_DECISION_PROCEDURE);
			err(e);
			return 1;
		} catch (InitializationException e) {
			err(ERROR_ENGINE_INIT_INITIAL_STATE);
			err(e);
			return 1;
        } catch (ClasspathException e) {
            err(ERROR_BAD_CLASSPATH);
            err(e);
            return 1;
        } catch (CannotBuildDecisionProcedureException e) {
            err(ERROR_DECISION_PROCEDURE_FAILED + e.getCause() + ".");
            return 1;
        } catch (CannotBuildEngineException e) {
            err(ERROR_BUILD_FAILED + e.getCause() + ".");
            return 2;
		} catch (InvalidClassFileFactoryClassException | UnexpectedInternalException e) {
			err(ERROR_UNEXPECTED);
			err(e);
			return 2;
		}
		
		return 0;
	}
	
	private void setStreams() {
        // sets the output and error streams
        // first are to standard
        this.out = new PrintStream[2];
        this.log = new PrintStream[2];
        this.err = new PrintStream[2];
        if (this.parameters.getShowOnConsole()) {
            this.out[0] = System.out;
            this.log[0] = System.err;
            this.err[0] = System.err;
        }

        // tries to open the dump file
        if (this.parameters.getOutputFileName() == null) {
            this.err[1] = null;
        } else {
            try {
                final File f = new File(this.parameters.getOutputFileName());
                this.err[1] = new PrintStream(f);
            } catch (FileNotFoundException | SecurityException e) {
                err(ERROR_DUMP_FILE_OPEN);
                this.err[1] = null;
            }
        }
        this.out[1] = this.log[1] = this.err[1];

        // sets line separator style
        if (this.parameters.getTextMode() == TextMode.WINDOWS) {
            System.setProperty("line.separator", "\r\n");
        } else if (this.parameters.getTextMode() == TextMode.UNIX) {
            System.setProperty("line.separator", "\n");
        } //else it is platform: nothing to do
	}
	
	/**
	 * Returns the engine's initial state.
	 * Convenience for formatter and 
	 * decision procedure creation.
	 * 
	 * @return the initial {@link State}.
	 */
    private State getInitialState() {
        return this.engine.getInitialState();
    }
    
    /**
     * Returns the engine's initial state.
     * Convenience for decision procedure creation.
     * 
     * @return the initial {@link State}.
     */
    private State getCurrentState() {
        return this.engine.getCurrentState();
    }
        
    /**
     * Returns the decision procedure's current 
     * model or {@code null}. Convenience for
     * formatter creation.
     * 
     * @return a {@link Map}{@code <}{@link PrimitiveSymbolic}{@code ,}{@link Simplex}{@code >}
     *         or {@code null} 
     */
    private Map<PrimitiveSymbolic, Simplex> getModel() {
        try {
            return this.decisionProcedure.getModel();
        } catch (DecisionException e) {
            return null;
        }
    }
    
    /**
     * Creates the formatter.
     * 
     * @throws CannotBuildFormatterException upon failure.
     */
    private void createFormatter() throws CannotBuildFormatterException {
        final StateFormatMode type = this.parameters.getStateFormatMode();
        if (type == StateFormatMode.FULLTEXT) {
            this.formatterBranches = new StateFormatterText(this.parameters.getSourcePath()) {
                @Override
                public void formatState(State s) {
                    this.output += LINE_SEP; // gutter
                    this.output += 
                        banner(s.getIdentifier() + "[" + s.getSequenceNumber() + "]", true);
                    this.output += LINE_SEP; // gutter
                    super.formatState(s);
                }
            };
            this.formatterOthers = new StateFormatterText(this.parameters.getSourcePath()) {
                @Override
                public void formatState(State s) {
                    this.output += LINE_SEP; // gutter
                    this.output += 
                        banner(s.getIdentifier() + "[" + s.getSequenceNumber() + "]", false);
                    this.output += LINE_SEP; // gutter
                    super.formatState(s);
                }
            };
        } else if (type == StateFormatMode.GRAPHVIZ) {
            this.formatterBranches = this.formatterOthers = new StateFormatterGraphviz();
        } else if (type == StateFormatMode.TRACE) {
            this.formatterBranches = this.formatterOthers = new StateFormatterTrace();
        } else if (type == StateFormatMode.JUNIT_TEST) {
            this.formatterBranches = this.formatterOthers = 
                new StateFormatterJUnitTestSuite(this::getInitialState, this::getModel);
        } else {
            throw new CannotBuildFormatterException(ERROR_UNDEF_STATE_FORMAT);
        }
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
	
    /**
     * Creates a {@link CalculatorRewriting}.
     * 
     * @return the {@link CalculatorRewriting}.
     * @throws CannotBuildEngineException upon failure 
     *         (cannot instantiate rewriters).
     */
	private CalculatorRewriting createCalculator() throws CannotBuildEngineException {
		final CalculatorRewriting calc;
		try {
			calc = new CalculatorRewriting();
			calc.addRewriter(new RewriterOperationOnSimplex()); //indispensable
			for (final Class<? extends Rewriter> rewriterClass : this.parameters.getRewriters()) {
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
	
	/**
	 * Creates the decision procedures in {@code this.decisionProcedure}
	 * and {@code this.decisionProcedureConcretization}. 
	 * 
	 * @param calc a {@link CalculatorRewriting}.
	 * @throws CannotBuildDecisionProcedureException upon failure.
	 */
	private void createDecisionProcedure(CalculatorRewriting calc)
	throws CannotBuildDecisionProcedureException {
        final Path path = this.parameters.getExternalDecisionProcedurePath();       

        //prints some feedback
        if (this.parameters.getShowInfo()) {
            if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.Z3) {
                log(MSG_TRY_Z3 + (path == null ? "default" : path.toString()) + ".");
            } else if (this.parameters.getDecisionProcedureType() == DecisionProcedureType.CVC4) {
                log(MSG_TRY_CVC4 + (path == null ? "default" : path.toString()) + ".");
            } else if (this.parameters.getInteractionMode() == InteractionMode.NO_INTERACTION) {
                log(MSG_DECISION_BASIC);
            } else {
                log(MSG_DECISION_INTERACTIVE);
            }
        }
	    
		//initializes cores
        final boolean needHeapCheck = (this.parameters.getUseConservativeRepOks() || this.parameters.getDoConcretization());
		DecisionProcedure core = new DecisionProcedureAlwSat();
		DecisionProcedure coreNumeric = (needHeapCheck ? new DecisionProcedureAlwSat() : null);
		
		//wraps cores with external numeric decision procedure
		final DecisionProcedureType type = this.parameters.getDecisionProcedureType();
		try {
		    if (type == DecisionProcedureType.ALL_SAT) {
		        //do nothing
		    } else if (type == DecisionProcedureType.Z3) {
		        final String z3 = (path == null ? "z3" : path.toString()) + COMMANDLINE_LAUNCH_Z3;
		        core = new DecisionProcedureSMTLIB2_AUFNIRA(core, calc, z3);
		        coreNumeric = (needHeapCheck ? new DecisionProcedureSMTLIB2_AUFNIRA(coreNumeric, calc, z3) : null);
		    } else if (type == DecisionProcedureType.CVC4) {
                final String cvc4 = (path == null ? "cvc4" : path.toString()) + COMMANDLINE_LAUNCH_CVC4;
		        core = new DecisionProcedureSMTLIB2_AUFNIRA(core, calc, cvc4);
		        coreNumeric = (needHeapCheck ? new DecisionProcedureSMTLIB2_AUFNIRA(coreNumeric, calc, cvc4) : null);
		    } else {
		        core.close();
		        if (coreNumeric != null) {
		            coreNumeric.close();
		        }
		        throw new CannotBuildDecisionProcedureException(ERROR_UNDEF_DECISION_PROCEDURE);
		    }
		} catch (DecisionException e) {
			throw new CannotBuildDecisionProcedureException(e);
		}
		
		//further wraps cores with sign analysis, if required
		if (this.parameters.getDoSignAnalysis()) {
			core = new DecisionProcedureSignAnalysis(core, calc);
			coreNumeric = (needHeapCheck ? new DecisionProcedureSignAnalysis(coreNumeric, calc) : null);
		}
		
		//further wraps cores with equality analysis, if required
		if (this.parameters.getDoEqualityAnalysis()) {
			core = new DecisionProcedureEquality(core, calc);
			coreNumeric = (needHeapCheck ? new DecisionProcedureEquality(coreNumeric, calc) : null);
		}
		
		//sets the decision procedure for checkers
		if (needHeapCheck) {
		    this.decisionProcedureConcretization = new DecisionProcedureAlgorithms(coreNumeric, calc);
		}
		
		//further wraps core with LICS decision procedure
		if (this.parameters.getUseLICS()) {
			core = new DecisionProcedureLICS(core, calc, this.parameters.getLICSRulesRepo());
		}
		
		//further wraps core with class init decision procedure
		core = new DecisionProcedureClassInit(core, calc, this.parameters.getClassInitRulesRepo());
		
		//further wraps core with conservative repOk decision procedure
		if (this.parameters.getUseConservativeRepOks()) {
		    final RunnerParameters checkerParameters = this.parameters.getConcretizationDriverParameters();
		    checkerParameters.setDecisionProcedure(this.decisionProcedureConcretization);
			@SuppressWarnings("resource")
            final DecisionProcedureConservativeRepOk dec = 
			    new DecisionProcedureConservativeRepOk(core, calc, checkerParameters, this.parameters.getConservativeRepOks());
            dec.setInitialStateSupplier(this::getInitialState); 
            dec.setCurrentStateSupplier(this::getCurrentState); 
			core = dec;
		}

		//wraps core with custom wrappers
		for (DecisionProcedureCreationStrategy c : this.parameters.getDecisionProcedureCreationStrategies()) {
			core = c.createAndWrap(core, calc);
		}

		//wraps with timer
		final DecisionProcedureDecoratorTimer tCore = new DecisionProcedureDecoratorTimer(core);
		this.timer = tCore;
		core = tCore;

		//wraps with printer if interaction with decision procedure must be shown
		if (this.parameters.getShowDecisionProcedureInteraction()) {
			core = new DecisionProcedureDecoratorPrint(core, out);
		}
				
		//finally guidance
		if (this.parameters.isGuided()) {
			final RunnerParameters guidanceDriverParameters = this.parameters.getGuidanceDriverParameters(calc);
			if (this.parameters.getShowInfo()) {
			    log(MSG_TRY_GUIDANCE + guidanceDriverParameters.getMethodSignature() + ".");
			}
			try {
			    if (this.parameters.getGuidanceType() == GuidanceType.JBSE) {
                                this.guidance = new DecisionProcedureGuidanceJBSE(core, calc, guidanceDriverParameters, this.parameters.getMethodSignature());
			    } else if (this.parameters.getGuidanceType() == GuidanceType.JDI) {
				this.guidance = new DecisionProcedureGuidanceJDI(core, calc, guidanceDriverParameters, this.parameters.getMethodSignature());
			    } else {
			        
			    }
			} catch (GuidanceException | UnexpectedInternalException e) {
				err(ERROR_GUIDANCE_FAILED + e.getMessage());
				throw new CannotBuildDecisionProcedureException(e);
			}
			core = this.guidance;
		}
		
		//sets the result
		this.decisionProcedure = ((core instanceof DecisionProcedureAlgorithms) ? 
		        (DecisionProcedureAlgorithms) core :
		        new DecisionProcedureAlgorithms(core, calc));
	}
	
	/**
	 * Creates the heap checker into {@code this.checker}.
	 * 
	 * @param decisionProcedureConcretization the {@link DecisionProcedureAlgorithms}
	 *        to be used by the heap checker.
	 */
	private void createHeapChecker(DecisionProcedureAlgorithms decisionProcedureConcretization) {
        if (this.parameters.getDoConcretization()) {
            final RunnerParameters checkerParameters = this.parameters.getConcretizationDriverParameters();
            checkerParameters.setDecisionProcedure(decisionProcedureConcretization);
            this.checker = 
                new InitialHeapChecker(checkerParameters, ConcretizationCheck.class, this.parameters.getConcretizationMethods());
            this.checker.setInitialStateSupplier(this::getInitialState); 
            this.checker.setCurrentStateSupplier(this::getCurrentState); 
        }
	}
	
	/**
	 * Emits the prologue of the symbolic execution.
	 */
	private void emitPrologue() {
        this.formatterOthers.cleanup();
        this.formatterOthers.formatPrologue();
        outNoBreak(this.formatterOthers.emit());
	}

	/**
	 * Emits a {@link State} of the symbolic execution.
	 * 
	 * @param s the {@link State} to be emitted.
	 * @param isRootBranch {@code true} iff 
	 *        {@code s} is at a branch point.
	 */
	private void emitState(State s, boolean isRootBranch) {
		final Formatter f = 
			(isRootBranch ? this.formatterBranches : this.formatterOthers);
        f.cleanup();
		f.formatState(s);
		outNoBreak(f.emit());
	}
    
    /**
     * Emits the epilogue of the symbolic execution.
     */
	private void emitEpilogue() {
        this.formatterOthers.cleanup();
        this.formatterOthers.formatEpilogue();
        outNoBreak(this.formatterOthers.emit());
	}
    
	/**
	 * Prints statistics.
	 */
    private void printFinalStats() {
        final long elapsedTime = this.runner.getStopTime() - this.runner.getStartTime();
        final long tracesContradictory = 
                this.runner.getTracesTotal() 
                - this.tracesSafe 
                - this.tracesUnsafe
                - this.runner.getTracesOutOfScope()
                - this.tracesUnmanageable;
        log(MSG_END_STATES + this.engine.getAnalyzedStates() + ", "
            + MSG_END_TRACES_TOT + this.runner.getTracesTotal() + ", "
            + MSG_END_TRACES_SAFE + this.tracesSafe 
            + (Run.this.parameters.getDoConcretization() ? 
                    " (" + this.tracesConcretizableSafe + " concretizable)"
                : "")
            + ", "
            + MSG_END_TRACES_UNSAFE + this.tracesUnsafe 
            + (Run.this.parameters.getDoConcretization() ? 
                    " (" + this.tracesConcretizableUnsafe + " concretizable)"
                : "")
            + ", "
            + MSG_END_TRACES_OUT_OF_SCOPE + this.runner.getTracesOutOfScope()
            + (Run.this.parameters.getDoConcretization() ? 
                    " (" + this.tracesConcretizableOutOfScope + " concretizable)"  
                : "")
            + ", "
            + MSG_END_TRACES_VIOLATING_ASSUMPTION + tracesContradictory
            + ", "
            + MSG_END_TRACES_UNMANAGEABLE + this.tracesUnmanageable + ".");
        final long elapsedTimeDecisionProcedure = (this.timer == null ? 0 : this.timer.getTime());
        log(MSG_END_ELAPSED + Util.formatTime(elapsedTime) + ", "
            + MSG_END_SPEED + this.engine.getAnalyzedStates() * 1000 / elapsedTime + " states/sec"
            + (Run.this.parameters.getDoConcretization() ? 
                    ", " + MSG_END_ELAPSED_CONCRETIZATION + Util.formatTime(elapsedTimeConcretization)
                    + " (" + Util.formatTimePercent(elapsedTimeConcretization, elapsedTime) + " of total)"
                  : "")
            + (this.timer == null ? 
                    "."
                  : ", " + MSG_END_DECISION + Util.formatTime(elapsedTimeDecisionProcedure) 
                    + " (" + Util.formatTimePercent(elapsedTimeDecisionProcedure, elapsedTime) + " of total)."
            ));
    }
    
    /**
     * Closes this {@link Run} object.
     * 
     * @return an {@code int} value representing an error code.
     */
    private int close() {
        int retVal = 0;
        
        // quits the numeric decision procedure for the checker
        if (this.decisionProcedureConcretization != null) {
            try {
                this.decisionProcedureConcretization.close();
                this.decisionProcedureConcretization = null;
                this.checker = null;
            } catch (DecisionException e) {
                err(ERROR_ENGINE_QUIT_DECISION_PROCEDURE);
                err(e);
                retVal = 1;
            }
        }

        // quits the engine
        try {
            this.engine.close();
        } catch (DecisionException e) {
            err(ERROR_ENGINE_QUIT_DECISION_PROCEDURE);
            err(e);
            retVal = 1;
        } catch (UnexpectedInternalException e) {
            err(ERROR_UNEXPECTED);
            err(e);
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
        
        return retVal;
    }

	// Private constants.

	/** Length of separator between text areas. */
	private static final int BANNER_LENGTH = 35;

	/** Char for separator between text areas. */
	private static final char BANNER_CHAR = '.';

	/** Message: welcome. */
	private static final String MSG_WELCOME_TXT = "This is the " + JBSE.NAME + "'s Run Tool (" + JBSE.ACRONYM + " v." + JBSE.VERSION +").";

	/** Message: trying to connect to Z3. */
	private static final String MSG_TRY_Z3 = "Connecting to Z3 at ";

    /** Message: trying to connect to CVC4. */
    private static final String MSG_TRY_CVC4 = "Connecting to CVC4 at ";

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
	
	/** Message: total unmanageable traces. */
	private static final String MSG_END_TRACES_UNMANAGEABLE = "Unmanageable: ";
	
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

	/** Warning: timeout. */
	private static final String WARNING_TIMEOUT = "Timeout.";

	/** Warning: exhausted heap scope. */
	private static final String WARNING_SCOPE_EXHAUSTED_HEAP = " trace exhausted heap scope.";

	/** Warning: exhausted depth scope. */
	private static final String WARNING_SCOPE_EXHAUSTED_DEPTH = " trace exhausted depth scope.";

	/** Warning: exhausted count scope. */
	private static final String WARNING_SCOPE_EXHAUSTED_COUNT = " trace exhausted count scope.";

	/** Warning: cannot manage a native method invocation. */
	private static final String WARNING_CANNOT_INVOKE_NATIVE = " met an unmanageable native method invocation.";

	/** Warning: cannot handle a bytecode. */
	private static final String WARNING_NOT_IMPLEMENTED_BYTECODE = " met a bytecode that it is not yet implemented.";

	/** Warning: the meta-level implementation is unsupported. */
	private static final String WARNING_META_UNSUPPORTED = " meta-level implementation of a method cannot be executed: ";

	/** Warning: a method call cannot be treated as returning an uninterpreted function value. */
	private static final String WARNING_UNINTERPRETED_UNSUPPORTED = " method call cannot be treated as returning an uninterpreted function symbolic value: ";

	/** Error: unable to open dump file. */
	private static final String ERROR_DUMP_FILE_OPEN = "Could not open the dump file. The session will be displayed on console only.";

	/** Error: unable to connect with decision procedure. */
	private static final String ERROR_DECISION_PROCEDURE_FAILED = "Connection failed, cause: ";

    /** Error: failed building symbolic executor. */
    private static final String ERROR_BUILD_FAILED = "Failed construction of symbolic executor, cause: ";

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
	private static final String ERROR_ENGINE_DECISION_PROCEDURE = "Unexpected failure of the decision procedure.";

	/**
	 * Error: unable to quit engine, because unable to quit the decision
	 * procedure.
	 */
	private static final String ERROR_ENGINE_QUIT_DECISION_PROCEDURE = "Unexpected internal error while quitting the decision procedure.";

    /** Error: unexpected internal error (undefined state format mode). */
    private static final String ERROR_UNDEF_STATE_FORMAT = "Unexpected internal error: This state format mode is unimplemented.";

    /** Error: unexpected internal error (undefined decision procedure). */
    private static final String ERROR_UNDEF_DECISION_PROCEDURE = "Unexpected internal error: This decision procedure is unimplemented.";

    /** Error: no or bad JRE. */
    private static final String ERROR_BAD_CLASSPATH = "No or incompatible JRE in the classpath.";

	/** Error: unexpected internal error (stepping while engine stuck). */
	private static final String ERROR_ENGINE_STUCK = "Unexpected internal error: Attempted step while in a stuck state.";

	/** Error: unexpected internal error. */
	private static final String ERROR_UNEXPECTED = "Unexpected internal error.";

	/** Prompt: ask user whether should continue execution by stepping. */
	private static final String PROMPT_SHOULD_STEP = "Proceed with next step? (x: abort, any other: step): ";

	/** Prompt: ask user whether should continue execution by backtracking. */
	private static final String PROMPT_SHOULD_BACKTRACK = "Trace finished, pending backtrack points; proceed with backtrack? (x: abort, any other: backtrack): ";
}