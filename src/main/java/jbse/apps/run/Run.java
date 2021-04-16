package jbse.apps.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
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
import jbse.apps.StateFormatterPath;
import jbse.apps.Timer;
import jbse.apps.Util;
import jbse.apps.run.RunParameters.DecisionProcedureCreationStrategy;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.GuidanceType;
import jbse.apps.run.RunParameters.InteractionMode;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.apps.run.RunParameters.TextMode;
import jbse.apps.run.RunParameters.PathTypes;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
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
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.meta.annotations.ConcretizationCheck;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterCalculatorRewriting;
import jbse.rewr.RewriterNegationElimination;
import jbse.rewr.RewriterExpressionOrConversionOnSimplex;
import jbse.rewr.RewriterFunctionApplicationOnSimplex;
import jbse.rewr.RewriterZeroUnit;
import jbse.tree.StateTree.BranchPoint;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
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
 * {@code Run} executes a path until it reaches a stuck state; then, if it
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

    /** The {@link Formatter} to output states. */
    private Formatter formatter = null;

    /** The {@link Timer} for the decision procedure. */
    private Timer timer = null;

    /** The {@link DecisionProcedureGuidance}, whenever this method is chosen for stepping the {@link Engine}. */
    private DecisionProcedureGuidance guidance = null;

    /** A purely numeric decision procedure for concretization checks. */
    private DecisionProcedureAlgorithms decisionProcedureConcretization = null;

    /** The concretization checker. */
    private InitialHeapChecker checker = null;

    /** Counter for the number of analyzed paths that are safe (do not violate assertions). */
    private long pathsSafe = 0;

    /** Counter for the number of analyzed paths that are unsafe (violate some assertion). */
    private long pathsUnsafe = 0;

    /** 
     * Counter for the number of analyzed paths that are unmanageable 
     * (the symbolic executor is not able to execute them). 
     */
    private long pathsUnmanageable = 0;

    /** Counter for the number of analyzed paths that are safe and concretizable. */
    private long pathsConcretizableSafe = 0;

    /** Counter for the number of analyzed paths that are unsafe and concretizable. */
    private long pathsConcretizableUnsafe = 0;

    /** Counter for the number of analyzed paths that are out of scope and concretizable. */
    private long pathsConcretizableOutOfScope = 0;

    /** The time spent during the concretization checks. */
    private long elapsedTimeConcretization = 0;
    
    /** Whether we are still in the pre-initial phase. */
    private boolean atPreInitialPhase = true;

    /** The timestamp of the end of the pre-initial phase. */
    private long timestampPreInitialPhaseEnd = 0;

    /** The number of states traversed during the pre-initial phase. */
    private long preInitialStateCount = 0;

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
        /** Increment the counter of the safe paths. */
        INC_SAFE,
        /** Increment the counter of the unsafe paths. */
        INC_UNSAFE,
        /** Increment the counter of the out of scope paths. */
        INC_OUT_OF_SCOPE
    }

    private class ActionsRun extends Runner.Actions {
        private String endOfPathMessage;
        private PathTypes pathKind;
        private boolean mayPrint;

        /**
         * Determines whether the stack size of the current state 
         * is below the maximum threshold for being printed.
         * 
         * @return {@code true} iff it is below the threshold.
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
            if (this.endOfPathMessage == null && this.pathKind != PathTypes.CONTRADICTORY && this.stackSizeAcceptable() && this.mayPrint) {
                try {
                    final State currentState = Run.this.getCurrentState();
                    Run.this.emitState(currentState);
                } catch (UnexpectedInternalException e) {
                    Run.this.err(ERROR_UNEXPECTED);
                    Run.this.err(e);
                    return true;
                }
            }

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
        public boolean atStart() {
            Run.this.emitPrologue();
            
            //enables or disables printing
            this.mayPrint = Run.this.parameters.getShowSystemClassesInitialization();

            //prints the state (all+bytecode and branches)
            boolean stop = false;
            if (Run.this.parameters.getStepShowMode() == StepShowMode.METHOD || 
                Run.this.parameters.getStepShowMode() == StepShowMode.SOURCE) {
                stop = printAndAsk();
            } 
            return stop;
        }
        
        @Override
        public boolean atInitial() {
        	Run.this.atPreInitialPhase = false;
        	Run.this.timestampPreInitialPhaseEnd = System.currentTimeMillis();
        	Run.this.preInitialStateCount = getEngine().getAnalyzedStates();
        	return super.atInitial();
        }
        
        @Override
        public boolean atPathStart() {
            //scope not yet exhausted
            this.endOfPathMessage = null;

            //path initially assumed to be safe
            this.pathKind = PathTypes.SAFE;

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
            boolean stop = false;
            if (Run.this.parameters.getStepShowMode() == StepShowMode.ROOT_BRANCHES_LEAVES) {
                stop = printAndAsk();
            } 
            return stop;
        }

        @Override
        public void atEnd() {
            Run.this.emitEpilogue();
            if (Run.this.atPreInitialPhase) {
            	//this means that an exception was raised during the
            	//pre-initial phase: fix the stats
            	Run.this.timestampPreInitialPhaseEnd = System.currentTimeMillis();
            	Run.this.preInitialStateCount = getEngine().getAnalyzedStates();
            }
            super.atEnd();
        }

        @Override
        public boolean atContradictionException(ContradictionException e) {
            this.pathKind = PathTypes.CONTRADICTORY;
            return false;
        }

        @Override
        public boolean atFailureException(FailureException e) {
            this.pathKind = PathTypes.UNSAFE;
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
            this.pathKind = PathTypes.OUT_OF_SCOPE;
            this.endOfPathMessage = WARNING_SCOPE_EXHAUSTED_HEAP;
            return super.atScopeExhaustionHeap();
        }

        @Override
        public boolean atScopeExhaustionDepth() {
            this.pathKind = PathTypes.OUT_OF_SCOPE;
            this.endOfPathMessage = WARNING_SCOPE_EXHAUSTED_DEPTH;
            return super.atScopeExhaustionDepth();
        }

        @Override
        public boolean atScopeExhaustionCount() {
            this.pathKind = PathTypes.OUT_OF_SCOPE;
            this.endOfPathMessage = WARNING_SCOPE_EXHAUSTED_COUNT;
            return super.atScopeExhaustionCount();
        }

        @Override
        public boolean atCannotManageStateException(CannotManageStateException e)
        throws CannotManageStateException {
            if (e instanceof CannotInvokeNativeException) {
                this.pathKind = PathTypes.UNMANAGEABLE;
                this.endOfPathMessage = WARNING_CANNOT_INVOKE_NATIVE + e.getMessage();
                return false;
            } else if (e instanceof NotYetImplementedException) {
                this.pathKind = PathTypes.UNMANAGEABLE;
                this.endOfPathMessage = WARNING_NOT_IMPLEMENTED_FEATURE + e.getMessage();
                return false;
            } else if (e instanceof MetaUnsupportedException) {
                this.pathKind = PathTypes.UNMANAGEABLE;
                this.endOfPathMessage = WARNING_META_UNSUPPORTED + e.getMessage();
                return false;
            } else if (e instanceof UninterpretedUnsupportedException) {
                this.pathKind = PathTypes.UNMANAGEABLE;
                this.endOfPathMessage = WARNING_UNINTERPRETED_UNSUPPORTED + e.getMessage();
                return false;
            } else {
                Run.this.err(ERROR_UNEXPECTED);
                Run.this.err(e);
                return true;
            }
        }

        @Override
        public boolean atStepPost() {
            final State currentState = Run.this.engine.getCurrentState();
            
            try {
            	if (Run.this.guidance != null) {
            		Run.this.guidance.step(currentState);
            	}
            } catch (GuidanceException e) {
            	Run.this.err(ERROR_UNEXPECTED);
            	Run.this.err(e);
            	return true;
            }
            
            //if a resolved reference has not been expanded, prints a warning
            if (Run.this.parameters.getShowWarnings() && 
                getEngine().someReferencePartiallyResolved()) {
                Run.this.log(currentState.getBranchIdentifier() + "[" + currentState.getSequenceNumber() + "]" + " " +
                             String.join(", ",getEngine().getPartiallyResolvedReferences().stream().map(ReferenceSymbolic::asOriginString).toArray(String[]::new)) +
                             WARNING_PARTIAL_REFERENCE_RESOLUTION);
            }
            
            //enables printing if we hit the root method execution
            if (Run.this.engine.atInitialState()) {
            	this.mayPrint = true;
            }

            //prints/asks (all+bytecode and branches)
            boolean stop = false;
            if (Run.this.parameters.getStepShowMode() == StepShowMode.ALL) {
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
        public boolean atPathEnd() {
            try {
                final State currentState = Run.this.engine.getCurrentState();
                //prints the leaf state if the case
                if (Run.this.parameters.getStepShowMode() == StepShowMode.ALL ||       //already shown
                    Run.this.parameters.getStepShowMode() == StepShowMode.SOURCE ||    //already shown
                    Run.this.parameters.getStepShowMode() == StepShowMode.METHOD ||    //already shown
                    Run.this.parameters.getStepShowMode() == StepShowMode.NONE   ||    //not to show
                    !Run.this.parameters.getPathsToShow().contains(this.pathKind)) { //not to show
                    //does nothing, the leaf state has been already printed 
                    //or must not be printed at all
                } else {
                    //prints the refined root state for the summaries case
                    if (Run.this.parameters.getStepShowMode() == StepShowMode.SUMMARIES) {
                        State initialRefined = Run.this.engine.getInitialState();
                        initialRefined.refine(currentState);
                        Run.this.emitState(initialRefined);
                        Run.this.out("\n===\n");
                    }
                    //prints the leaf (stuck) state
                    Run.this.emitState(currentState);
                } 

                //displays path end message and updates stats
                final CounterKind counterKind;
                switch (this.pathKind) {
                case SAFE:
                    ++Run.this.pathsSafe;
                    this.endOfPathMessage = MSG_PATH_SAFE;
                    counterKind = CounterKind.INC_SAFE;
                    break;
                case UNSAFE:
                    ++Run.this.pathsUnsafe;
                    this.endOfPathMessage = MSG_PATH_UNSAFE;
                    counterKind = CounterKind.INC_UNSAFE;
                    break;
                case OUT_OF_SCOPE:
                    //counter is provided by runner
                    //this.endOfPathMessage already set
                    counterKind = CounterKind.INC_OUT_OF_SCOPE;
                    break;
                case UNMANAGEABLE:
                    ++Run.this.pathsUnmanageable;
                    //this.endOfPathMessage already set
                    counterKind = null;
                    break;
                case CONTRADICTORY:
                    this.endOfPathMessage = MSG_PATH_CONTRADICTORY;
                    counterKind = null;
                    break;
                default: //to keep compiler happy:
                    throw new AssertionError();
                }
                if (Run.this.parameters.getShowWarnings()) {
                    Run.this.log(currentState.getBranchIdentifier() + "[" + currentState.getSequenceNumber() + "]" + this.endOfPathMessage);
                }
                if (Run.this.parameters.getDoConcretization()) {
                    checkFinalStateIsConcretizable(counterKind);
                }

            } catch (CannotRefineException | FrozenStateException e) {
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
        
        @Override
        public boolean atNonexistingObservedVariablesException(NonexistingObservedVariablesException e)
        throws NonexistingObservedVariablesException {
            for (int i : e.getVariableIndices()) {
                if (Run.this.parameters.getShowWarnings()) {
                	Run.this.log(WARNING_PARAMETERS_UNRECOGNIZABLE_VARIABLE + i
                        + (i == 1 ? "-st." : i == 2 ? "-nd." : i == 3 ? "-rd." : "-th."));
                }
            }
        	return super.atNonexistingObservedVariablesException(e);
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
                    ++Run.this.pathsConcretizableOutOfScope;
                } else if (ctr == CounterKind.INC_SAFE) {
                    ++Run.this.pathsConcretizableSafe;
                } else { //ctr == CounterKind.INC_UNSAFE
                    ++Run.this.pathsConcretizableUnsafe;
                }
            }
            if (Run.this.parameters.getShowWarnings()) {
                final State currentState = Run.this.engine.getCurrentState();
                Run.this.log(currentState.getBranchIdentifier() +
                             (concretizable ? MSG_PATH_CONCRETIZABLE : MSG_PATH_NOT_CONCRETIZABLE));
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
                 EngineStuckException | CannotBacktrackException | 
                 NonexistingObservedVariablesException e) {
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
            final RunnerParameters runnerParameters = this.parameters.getRunnerParameters();
            runnerParameters.setActions(new ActionsRun());
            final CalculatorRewriting calc = createCalculator();
            runnerParameters.setCalculator(calc);
            createDecisionProcedure(calc);
            runnerParameters.setDecisionProcedure(this.decisionProcedure);
            final RunnerBuilder rb = new RunnerBuilder();
            this.runner = rb.build(this.parameters.getRunnerParameters());
            this.engine = rb.getEngine();
            if (this.engine == null) {
                return 1;
            }
            createHeapChecker(this.decisionProcedureConcretization);
            createFormatter();
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
        } catch (NotYetImplementedException e) {
            err(ERROR_NOT_IMPLEMENTED_FEATURE_DURING_INIT + e.getCause() + ".");
            return 1;
        } catch (ContradictionException e) {
            err(ERROR_CONTRADICTION_DURING_INIT + e.getCause() + ".");
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
        if (this.parameters.getOutputFilePath() == null) {
            this.err[1] = null;
        } else {
            try {
                final File f = this.parameters.getOutputFilePath().toFile();
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
            this.formatter = new StateFormatterText(this.parameters.getSourcePath(), true);
        } else if (type == StateFormatMode.TEXT) {
            this.formatter = new StateFormatterText(this.parameters.getSourcePath(), false);
        } else if (type == StateFormatMode.GRAPHVIZ) {
            this.formatter = new StateFormatterGraphviz();
        } else if (type == StateFormatMode.PATH) {
            this.formatter = new StateFormatterPath();
        } else if (type == StateFormatMode.JUNIT_TEST) {
            this.formatter = new StateFormatterJUnitTestSuite(this::getInitialState, this::getModel);
        } else {
            throw new CannotBuildFormatterException(ERROR_UNDEF_STATE_FORMAT);
        }
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
            calc.addRewriter(new RewriterExpressionOrConversionOnSimplex()); //indispensable
            calc.addRewriter(new RewriterFunctionApplicationOnSimplex()); //indispensable
    		calc.addRewriter(new RewriterZeroUnit()); //indispensable
    		calc.addRewriter(new RewriterNegationElimination()); //indispensable?
            for (final Class<? extends RewriterCalculatorRewriting> rewriterClass : this.parameters.getRewriters()) {
                if (rewriterClass == null) { 
                    //no rewriter
                    continue; 
                }
                final RewriterCalculatorRewriting rewriter = (RewriterCalculatorRewriting) rewriterClass.newInstance();
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
    	try {
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
    		DecisionProcedure core = new DecisionProcedureAlwSat(calc);
    		DecisionProcedure coreNumeric = (needHeapCheck ? new DecisionProcedureAlwSat(calc) : null);

    		//wraps cores with external numeric decision procedure
    		final DecisionProcedureType type = this.parameters.getDecisionProcedureType();
    		try {
    			if (type == DecisionProcedureType.ALL_SAT) {
    				//do nothing
    			} else if (type == DecisionProcedureType.Z3) {
    				final String switchChar = System.getProperty("os.name").toLowerCase().contains("windows") ? "/" : "-";
    				final ArrayList<String> z3CommandLine = new ArrayList<>();
    				z3CommandLine.add(path == null ? "z3" : path.toString());
    				z3CommandLine.add(switchChar + "smt2");
    				z3CommandLine.add(switchChar + "in");
    				z3CommandLine.add(switchChar + "t:10");
    				core = new DecisionProcedureSMTLIB2_AUFNIRA(core, z3CommandLine);
    				coreNumeric = (needHeapCheck ? new DecisionProcedureSMTLIB2_AUFNIRA(coreNumeric, z3CommandLine) : null);
    			} else if (type == DecisionProcedureType.CVC4) {
    				final ArrayList<String> cvc4CommandLine = new ArrayList<>();
    				cvc4CommandLine.add(path == null ? "cvc4" : path.toString());
    				cvc4CommandLine.add("--lang=smt2");
    				cvc4CommandLine.add("--output-lang=smt2");
    				cvc4CommandLine.add("--no-interactive");
    				cvc4CommandLine.add("--incremental");
    				cvc4CommandLine.add("--tlimit-per=10000");
    				core = new DecisionProcedureSMTLIB2_AUFNIRA(core, cvc4CommandLine);
    				coreNumeric = (needHeapCheck ? new DecisionProcedureSMTLIB2_AUFNIRA(coreNumeric, cvc4CommandLine) : null);
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
    			core = new DecisionProcedureSignAnalysis(core);
    			coreNumeric = (needHeapCheck ? new DecisionProcedureSignAnalysis(coreNumeric) : null);
    		}

    		//further wraps cores with equality analysis, if required
    		if (this.parameters.getDoEqualityAnalysis()) {
    			core = new DecisionProcedureEquality(core);
    			coreNumeric = (needHeapCheck ? new DecisionProcedureEquality(coreNumeric) : null);
    		}

    		//sets the decision procedure for checkers
    		if (needHeapCheck) {
    			this.decisionProcedureConcretization = new DecisionProcedureAlgorithms(coreNumeric);
    		}

    		//further wraps core with LICS decision procedure
    		if (this.parameters.getUseLICS()) {
    			core = new DecisionProcedureLICS(core, this.parameters.getLICSRulesRepo());
    		}

    		//further wraps core with class init decision procedure
    		core = new DecisionProcedureClassInit(core, this.parameters.getClassInitRulesRepo());

    		//further wraps core with conservative repOk decision procedure
    		if (this.parameters.getUseConservativeRepOks()) {
    			final RunnerParameters checkerParameters = this.parameters.getConcretizationDriverParameters();
    			checkerParameters.setDecisionProcedure(this.decisionProcedureConcretization);
    			@SuppressWarnings("resource")
    			final DecisionProcedureConservativeRepOk dec = 
    			new DecisionProcedureConservativeRepOk(core, checkerParameters, this.parameters.getConservativeRepOks());
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
    					this.guidance = new DecisionProcedureGuidanceJBSE(core, calc, guidanceDriverParameters, this.parameters.getMethodSignature(), this.parameters.getGuidedNumberOfHits());
    				} else if (this.parameters.getGuidanceType() == GuidanceType.JDI) {
    					this.guidance = new DecisionProcedureGuidanceJDI(core, calc, guidanceDriverParameters, this.parameters.getMethodSignature(), this.parameters.getGuidedNumberOfHits());
    				} else {
    					throw new UnexpectedInternalException(ERROR_DECISION_PROCEDURE_GUIDANCE_UNRECOGNIZED + this.parameters.getGuidanceType().toString());
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
    					new DecisionProcedureAlgorithms(core));
    	} catch (InvalidInputException e) {
    		//this should never happen
    		throw new UnexpectedInternalException(e);
    	}
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
        this.formatter.cleanup();
        this.formatter.formatPrologue();
        outNoBreak(this.formatter.emit());
    }

    /**
     * Emits a {@link State} of the symbolic execution.
     * 
     * @param s the {@link State} to be emitted.
     * @param isRootBranch {@code true} iff 
     *        {@code s} is at a branch point.
     */
    private void emitState(State s) {
        this.formatter.cleanup();
        this.formatter.formatState(s);
        outNoBreak(this.formatter.emit());
    }

    /**
     * Emits the epilogue of the symbolic execution.
     */
    private void emitEpilogue() {
        this.formatter.cleanup();
        this.formatter.formatEpilogue();
        outNoBreak(this.formatter.emit());
    }

    /**
     * Prints statistics.
     */
    private void printFinalStats() {
        final long elapsedTime = this.runner.getStopTime() - this.runner.getStartTime();
        final long elapsedTimePreInitialPhase = (this.timestampPreInitialPhaseEnd - this.runner.getStartTime());
        final long elapsedTimeDecisionProcedure = (this.timer == null ? 0 : this.timer.getTime());
        final long speed = this.engine.getAnalyzedStates() * 1000 / elapsedTime;
        final long speedPostInitialPhase = (elapsedTime == elapsedTimePreInitialPhase) ? 0 : (this.engine.getAnalyzedStates() - this.preInitialStateCount) * 1000 / (elapsedTime - elapsedTimePreInitialPhase);
        final long pathsViolatingAssumptions = 
            this.runner.getPathsTotal() -
            this.pathsSafe - 
            this.pathsUnsafe -
            this.runner.getPathsOutOfScope() -
            this.pathsUnmanageable;
        log(MSG_END_STATES + this.engine.getAnalyzedStates() + ", " +
        	MSG_END_STATES_PREINITIAL + this.preInitialStateCount + ", " +
            MSG_END_PATHS_TOT + this.runner.getPathsTotal() + ", " +
            MSG_END_PATHS_SAFE + this.pathsSafe + 
            (Run.this.parameters.getDoConcretization() ? 
             " (" + this.pathsConcretizableSafe + " concretizable)" :
             "") +
            ", " +
            MSG_END_PATHS_UNSAFE + this.pathsUnsafe + 
            (Run.this.parameters.getDoConcretization() ? 
             " (" + this.pathsConcretizableUnsafe + " concretizable)" :
             "") +
            ", " +
            MSG_END_PATHS_OUT_OF_SCOPE + this.runner.getPathsOutOfScope() +
            (Run.this.parameters.getDoConcretization() ? 
             " (" + this.pathsConcretizableOutOfScope + " concretizable)" :  
             "") +
            ", " +
            MSG_END_PATHS_VIOLATING_ASSUMPTION + pathsViolatingAssumptions +
            ", " +
            MSG_END_PATHS_UNMANAGEABLE + this.pathsUnmanageable + ".");
        log(MSG_END_ELAPSED + Util.formatTime(elapsedTime) + ", " +
        	MSG_END_ELAPSED_PREINITIAL + Util.formatTime(elapsedTimePreInitialPhase) + ", " +
            MSG_END_SPEED + speed + " states/sec, " +
            MSG_END_SPEED_POSTINITIAL + speedPostInitialPhase + " states/sec" +
            (Run.this.parameters.getDoConcretization() ? 
             ", " + MSG_END_ELAPSED_CONCRETIZATION + Util.formatTime(this.elapsedTimeConcretization) + " (" + Util.formatTimePercent(this.elapsedTimeConcretization, elapsedTime) + " of total)" :
             "") +
            (this.timer == null ? 
             "." :
             ", " + MSG_END_DECISION + Util.formatTime(elapsedTimeDecisionProcedure) + " (" + Util.formatTimePercent(elapsedTimeDecisionProcedure, elapsedTime) + " of total)."));
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

    /** Message: the path is safe. */
    private static final String MSG_PATH_SAFE = " path is safe.";

    /** Message: the path is unsafe (violated an assertion). */
    private static final String MSG_PATH_UNSAFE = " path violates an assertion.";

    /** Message: the path is contradictory/irrelevant (violated an assumption). */
    private static final String MSG_PATH_CONTRADICTORY = " path violates an assumption.";

    /** Message: the path is concretizable. */
    private static final String MSG_PATH_CONCRETIZABLE = " path has a concretizable final state.";

    /** Message: the path is not concretizable. */
    private static final String MSG_PATH_NOT_CONCRETIZABLE = " path has not a concretizable final state.";

    /** Message: end of symbolic execution. */
    private static final String MSG_END = "Symbolic execution finished at ";

    /** Message: elapsed time. */
    private static final String MSG_END_ELAPSED = "Elapsed time: ";

    /** Message: elapsed time during the pre-initial phase. */
    private static final String MSG_END_ELAPSED_PREINITIAL = "Elapsed pre-initial phase time: ";

    /** Message: elapsed time in the concretization. */
    private static final String MSG_END_ELAPSED_CONCRETIZATION = "Elapsed concretization time: ";

    /** Message: elapsed time in the decision procedure. */
    private static final String MSG_END_DECISION = "Elapsed time in decision procedure: ";

    /** Message: average speed. */
    private static final String MSG_END_SPEED = "Average speed: ";

    /** Message: average speed. */
    private static final String MSG_END_SPEED_POSTINITIAL = "Average post-initial phase speed: ";

    /** Message: analyzed states. */
    private static final String MSG_END_STATES = "Analyzed states: ";

    /** Message: analyzed pre-initial states. */
    private static final String MSG_END_STATES_PREINITIAL = "Analyzed pre-initial states: ";

    /** Message: total paths. */
    private static final String MSG_END_PATHS_TOT = "Analyzed paths: ";

    /** Message: total paths violating assumptions. */
    private static final String MSG_END_PATHS_VIOLATING_ASSUMPTION = "Violating assumptions: ";

    /** Message: total unmanageable paths. */
    private static final String MSG_END_PATHS_UNMANAGEABLE = "Unmanageable: ";

    /** Message: total safe paths. */
    private static final String MSG_END_PATHS_SAFE = "Safe: ";

    /** Message: total unsafe paths. */
    private static final String MSG_END_PATHS_UNSAFE = "Unsafe: ";

    /** Message: total paths. */
    private static final String MSG_END_PATHS_OUT_OF_SCOPE = "Out of scope: ";

    /** Message: will consider all the clauses satisfiable. */
    private static final String MSG_DECISION_BASIC = "Will use a noninteractive, always-sat decision procedure when necessary.";

    /** Message: will ask to the user whether clauses are satisfiable or not. */
    private static final String MSG_DECISION_INTERACTIVE = "Will query via console about the satisfiability of a clause when necessary.";

    /** Warning: unrecognizable signature. */
    private static final String WARNING_PARAMETERS_UNRECOGNIZABLE_VARIABLE = "Unrecognizable variable will not be observed: ";

    /** Warning: partial reference resolution (part 2). */
    private static final String WARNING_PARTIAL_REFERENCE_RESOLUTION = " not expanded, because no concrete, compatible, pre-initialized " + 
    "class was found.";

    /** Warning: timeout. */
    private static final String WARNING_TIMEOUT = "Timeout.";

    /** Warning: exhausted heap scope. */
    private static final String WARNING_SCOPE_EXHAUSTED_HEAP = " path exhausted heap scope.";

    /** Warning: exhausted depth scope. */
    private static final String WARNING_SCOPE_EXHAUSTED_DEPTH = " path exhausted depth scope.";

    /** Warning: exhausted count scope. */
    private static final String WARNING_SCOPE_EXHAUSTED_COUNT = " path exhausted count scope.";

    /** Warning: cannot manage a native method invocation. */
    private static final String WARNING_CANNOT_INVOKE_NATIVE = " performed an unmanageable native method invocation: ";

    /** Warning: cannot handle something. */
    private static final String WARNING_NOT_IMPLEMENTED_FEATURE = " met an unimplemented feature: ";

    /** Warning: the meta-level implementation is unsupported. */
    private static final String WARNING_META_UNSUPPORTED = " meta-level implementation of a method cannot be executed: ";

    /** Warning: a method call cannot be treated as returning an uninterpreted function value. */
    private static final String WARNING_UNINTERPRETED_UNSUPPORTED = " method call cannot be treated as returning an uninterpreted function symbolic value: ";

    /** Error: unable to open dump file. */
    private static final String ERROR_DUMP_FILE_OPEN = "Could not open the dump file. The session will be displayed on console only.";

    /** Error: unable to connect with decision procedure. */
    private static final String ERROR_DECISION_PROCEDURE_FAILED = "Connection failed, cause: ";

    /** Error: unrecognized guidance decision procedure type. */
    private static final String ERROR_DECISION_PROCEDURE_GUIDANCE_UNRECOGNIZED = "Unrecognized guidance decision procedure type ";

    /** Error: failed building symbolic executor. */
    private static final String ERROR_BUILD_FAILED = "Failed construction of symbolic executor, cause: ";

    /** Error: failed guidance. */
    private static final String ERROR_GUIDANCE_FAILED = "Failed guidance, cause: ";

    /** Error: cannot handle something. */
    private static final String ERROR_NOT_IMPLEMENTED_FEATURE_DURING_INIT = " met an unimplemented feature during initialization: ";

    /** Error: cannot handle something. */
    private static final String ERROR_CONTRADICTION_DURING_INIT = " some class initialization assumption was violated during initialization: ";

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
    private static final String ERROR_BAD_CLASSPATH = "Cannot find item in the classpath.";

    /** Error: unexpected internal error (stepping while engine stuck). */
    private static final String ERROR_ENGINE_STUCK = "Unexpected internal error: Attempted step while in a stuck state.";

    /** Error: unexpected internal error. */
    private static final String ERROR_UNEXPECTED = "Unexpected internal error.";

    /** Prompt: ask user whether should continue execution by stepping. */
    private static final String PROMPT_SHOULD_STEP = "Proceed with next step? (x: abort, any other: step): ";

    /** Prompt: ask user whether should continue execution by backtracking. */
    private static final String PROMPT_SHOULD_BACKTRACK = "At the end of the path, pending backtrack points; proceed with backtrack? (x: abort, any other: backtrack): ";
}