package jbse.jvm;

import static jbse.val.HistoryPoint.BRANCH_IDENTIFIER_SEPARATOR_COMPACT;
import static jbse.val.HistoryPoint.BRANCH_IDENTIFIER_SEPARATOR_LONG;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionBacktrackException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.RunnerParameters.ScopeLoopsItem;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.StateTree.BranchPoint;

/**
 * Class implementing an algorithm for fully running a Java method by suitably 
 * steering an {@link Engine}.
 * 
 * @author Pietro Braione
 */
public class Runner {
    /**
     * Class collecting the actions to be performed at specific situations 
     * while a method is run; the actions invoked by a {@link Runner} in the 
     * context of the execution of the {@link Runner#run run} method.
     * 
     * @author Pietro Braione
     *
     */
    public static class Actions {
        private Engine engine;

        protected Engine getEngine() { return this.engine; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever it is at the root (post-init state, either pre-initial
         * or initial). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atStart() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever it is at the initial state. 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atInitial() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever it is at 
         * the start of a path, i.e., at root or after a successful backtrack. 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atPathStart() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method before 
         * a step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atStepPre() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method before 
         * a step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}), when this step is the first step of a source code statement 
         * or a frame changer (method invocation, throw exception, return). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atSourceRowPre() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method before 
         * a step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}), when this step is the first step of a method invocation
         * or a frame changer (method invocation, throw exception, return). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atMethodPre() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever after a step the heap scope has been exhausted.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atScopeExhaustionHeap() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever after a step the depth scope has been exhausted.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atScopeExhaustionDepth() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever after a step the count scope has been exhausted.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atScopeExhaustionCount() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever after a step the stack scope has been exhausted.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atScopeExhaustionStack() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever after a step the loops scope has been exhausted.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atScopeExhaustionLoops() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method 
         * whenever execution times out.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public void atTimeout() { }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method after 
         * a step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}) when the step created a new branch.
         * By default returns {@code false}.
         *  
         * @param bp the {@link BranchPoint} returned by the step.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atBranch(BranchPoint bp) { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method after 
         * a successful (i.e., no exception thrown) or unsuccessful (i.e., exception thrown) 
         * step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}), when this step is the first step of a method invocation 
         * or a frame changer (method invocation, throw exception, return). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atMethodPost() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method after 
         * a successful (i.e., no exception thrown) or unsuccessful (i.e., exception thrown) 
         * step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}), when this step is the first step of a source code statement
         * or a frame changer (method invocation, throw exception, return). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atSourceRowPost() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method after 
         * a successful (i.e., no exception thrown) or unsuccessful (i.e., exception thrown) 
         * step (note that this implies that a step is possible, i.e., {@link Engine.canStep()} 
         * returns {@code true}). 
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atStepPost() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method when at the end
         * of a path (i.e., when the current {@link State} is stuck).
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atPathEnd() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method immediately before
         * backtracking (note that this implies that backtrack is possible, i.e., 
         * {@link Engine.canBacktrack()} returns {@code true}).
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atBacktrackPre() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method immediately after
         * successful (i.e., exception thrown) or unsuccessful (i.e., no exception thrown) 
         * backtracking (note that this  implies that backtrack is possible, i.e., 
         * {@link Engine.canBacktrack()} returns {@code true}).
         * By default returns {@code false}.
         * 
         * @param bp the {@link BranchPoint} returned by the backtrack.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atBacktrackPost(BranchPoint bp) { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever it is at 
         * the end of the execution. By default does nothing.
         */
        public void atEnd() { }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link DecisionException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link DecisionException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws DecisionException by default.
         */
        public boolean atDecisionException(DecisionException e) 
        throws DecisionException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link EngineStuckException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link EngineStuckException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws EngineStuckException by default.
         */
        public boolean atEngineStuckException(EngineStuckException e) 
        throws EngineStuckException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link ContradictionException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link ContradictionException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws ContradictionException by default.
         */
        public boolean atContradictionException(ContradictionException e) 
        throws ContradictionException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link FailureException} is thrown by the {@link Engine}. 
         * By default returns {@code false}.
         * 
         * @param e the {@link FailureException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws FailureException.
         */
        public boolean atFailureException(FailureException e) 
        throws FailureException { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link ThreadStackEmptyException} is thrown by the {@link Engine}. 
         * By default returns {@code false}.
         * 
         * @param e the {@link ThreadStackEmptyException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws ThreadStackEmptyException.
         */
        public boolean atThreadStackEmptyException(ThreadStackEmptyException e) 
        throws ThreadStackEmptyException { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link NonexistingObservedVariablesException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link NonexistingObservedVariablesException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws NonexistingObservedVariablesException by default.
         */
        public boolean atNonexistingObservedVariablesException(NonexistingObservedVariablesException e) 
        throws NonexistingObservedVariablesException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run}  method whenever a 
         * {@link CannotManageStateException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link CannotManageStateException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws CannotManageStateException by default.
         */
        public boolean atCannotManageStateException(CannotManageStateException e) 
        throws CannotManageStateException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run}  method whenever a 
         * {@link ClasspathException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link ClasspathException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws ClasspathException by default.
         */
        public boolean atClasspathException(ClasspathException e) 
        throws ClasspathException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link DecisionBacktrackException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link DecisionBacktrackException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws DecisionBacktrackException by default.
         */
        public boolean atDecisionBacktrackException(DecisionBacktrackException e) 
        throws DecisionBacktrackException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method whenever a 
         * {@link CannotBacktrackException} is thrown by the {@link Engine}. 
         * By default rethrows the exception.
         * 
         * @param e the {@link CannotBacktrackException} thrown by the {@link Engine}.
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         * @throws CannotBacktrackException by default.
         */
        public boolean atCannotBacktrackException(CannotBacktrackException e) 
        throws CannotBacktrackException { throw e; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method after a 
         * step, both successful and unsuccessful (exception thrown).
         * If step is successful, it is invoked after {@link atStepPost}.
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atStepFinally() { return false; }

        /**
         * Invoked by a {@link Runner}'s {@link Runner#run run} method after a 
         * backtrack, both successful and unsuccessful (exception thrown).
         * If backtrack is successful, it is invoked after {@link atBacktrackPost}.
         * By default returns {@code false}.
         * 
         * @return {@code true} iff the {@link Runner} must stop
         *         {@link Runner#run run}ning.
         */
        public boolean atBacktrackFinally() { return false; }
    }

    /** The symbolic execution engine used by the {@link Runner}. */
    private final Engine engine;

    /** The {@link Actions} to be performed while {@link #run}ning. */
    private final Actions actions;

    /** The identifier of the branch state in the state space subregion we want to explore (null for everything). */
    private String identifierSubregion;

    /** The heap scope. */
    private final Map<String, Integer> heapScope;

    /** The depth scope. */
    private final int depthScope;

    /** The count scope. */
    private final int countScope;

    /** The stack scope. */
    private final int stackScope;
    
    /** The loops scope. */
    private final List<ScopeLoopsItem> loopsScope;

    /** The timeout. */
    private long timeout;

    /** Counter for the total number of analyzed paths. */
    private long pathsTot;

    /** Counter for the number of analyzed paths stopped because of scope exhaustion. */
    private long pathsOutOfScope;

    /** Stores the start time. */
    private long startTime;

    /** Stores the stop time. */
    private long stopTime;

    /**
     * Constructor.
     * 
     * @param engine the {@link Engine} which will be driven. It must
     *        be suitably initialized.
     * @param actions the {@link Actions} to be performed while 
     *        {@link #run}ning.
     * @param identifierSubregion a {@link String}, the identifier 
     *        of the subregion of the state space that will be explored
     *        by the execution.
     * @param timeout a timeout for the execution, in milliseconds (zero
     *        means unlimited time).
     * @param heapScope the heap scope, a 
     *        {@link Map}{@code <}{@link String}{@code , }{@link Integer}{@code >}
     *        mapping class names with their respective scopes ({@code <= 0} means unlimited).
     * @param depthScope the depth scope, an {@code int} ({@code <= 0} means unlimited).
     * @param countScope the count scope, an {@code int} ({@code <= 0} means unlimited).
     * @param countScope the stack scope, an {@code int} ({@code <= 0} means unlimited).
     */
    Runner(Engine engine, 
           Actions actions, 
           String identifierSubregion, 
           long timeout, 
           Map<String, Integer> heapScope, 
           int depthScope, 
           int countScope,
           int stackScope,
           List<ScopeLoopsItem> loopsScope) {
        this.engine = engine;
        this.actions = actions;
        this.actions.engine = engine;
        this.identifierSubregion = identifierSubregion;
        this.timeout = timeout;
        this.heapScope = heapScope;
        this.depthScope = depthScope;
        this.countScope = countScope;
        this.stackScope = stackScope;
        this.loopsScope = loopsScope;
        this.pathsOutOfScope = 0;
        this.pathsTot = 0;
    }
    
    public Engine getEngine() {
    	return this.engine;
    }

    private boolean currentStateIsInRunSubregion() {
        if (this.identifierSubregion == null) {
            return true;
        }
        final String currentRegion = this.engine.getCurrentState().getBranchIdentifier();
        final boolean retVal = (this.identifierSubregion.equals(currentRegion) ||
                                currentRegion.startsWith(this.identifierSubregion + BRANCH_IDENTIFIER_SEPARATOR_COMPACT) || 
                                currentRegion.startsWith(this.identifierSubregion + BRANCH_IDENTIFIER_SEPARATOR_LONG) || 
                                this.identifierSubregion.startsWith(currentRegion + BRANCH_IDENTIFIER_SEPARATOR_COMPACT) ||
                                this.identifierSubregion.startsWith(currentRegion + BRANCH_IDENTIFIER_SEPARATOR_LONG));
        return retVal;
    }

    private boolean outOfScope() {
        return (outOfScopeHeap() || outOfScopeDepth() || outOfScopeCount() || outOfScopeStack() || outOfScopeLoops());
    }

    private boolean outOfScopeHeap() {
        for (String className : this.heapScope.keySet()) {
            final int scope = this.heapScope.get(className);
            final int numAssumed = this.engine.getNumAssumed(className);
            if (numAssumed > scope) {
                return true;
            }
        }
        return false;
    }

    private boolean outOfScopeDepth() {
        final boolean retVal = (this.depthScope > 0 && this.engine.getCurrentState().getDepth() > this.depthScope);
        return retVal;
    }

    private boolean outOfScopeCount() {
        final boolean retVal = (this.countScope > 0 && this.engine.getCurrentState().phase() == Phase.POST_INITIAL && this.engine.getCurrentState().getCount() > this.countScope);
        return retVal;
    }

    private boolean outOfScopeStack() {
        final boolean retVal = (this.stackScope > 0 && this.engine.getCurrentState().phase() == Phase.POST_INITIAL && this.engine.getCurrentState().getStackSize() > this.stackScope);
        return retVal;
    }

    private boolean outOfScopeLoops() {
    	if (this.loopsScope.isEmpty()) {
    		return false;
    	}
    	final State currentState = this.engine.getCurrentState();
    	if (currentState.phase() != Phase.POST_INITIAL) {
    		return false;
    	}
    	try {
    		final String currentMethodClass = currentState.getCurrentMethodSignature().getClassName();
    		final String currentMethodDescriptor = currentState.getCurrentMethodSignature().getDescriptor();
    		final String currentMethodName = currentState.getCurrentMethodSignature().getName();
    		final int backjumps = currentState.getCurrentFrameBackjumps();
    		for (ScopeLoopsItem item : this.loopsScope) {
    			final Matcher matcherClass = item.patternClass.matcher(currentMethodClass);
    			final Matcher matcherDescriptor = item.patternDescriptor.matcher(currentMethodDescriptor);
    			final Matcher matcherName = item.patternName.matcher(currentMethodName);
    			if (matcherClass.matches() && matcherDescriptor.matches() && matcherName.matches() &&
    			backjumps > item.scopeLoops) {
    				return true;
    			}
    		}
    		return false;
    	} catch (ThreadStackEmptyException e) {
    		return false;
    	}
    }

    /**
     * Runs the method.
     * 
     * @throws CannotBacktrackException as in {@link Engine#backtrack()} 
     * @throws CannotManageStateException as in {@link Engine#step()} 
     * @throws ClasspathException as in {@link Engine#step()} 
     * @throws ThreadStackEmptyException as in {@link Engine#step()}
     * @throws ContradictionException as in {@link Engine#step()} 
     * @throws DecisionException as in {@link Engine#step()} 
     * @throws EngineStuckException as in {@link Engine#step()} 
     * @throws FailureException as in {@link Engine#step()} 
     * @throws NonexistingObservedVariablesException as in {@link Engine#step()} 
     */
    public void run() 
    throws CannotBacktrackException, CannotManageStateException, 
    ClasspathException, ThreadStackEmptyException, 
    ContradictionException, DecisionException, EngineStuckException, 
    FailureException, NonexistingObservedVariablesException  {
        this.startTime = System.currentTimeMillis();

        try {
            doRun();
        } finally {
            this.stopTime = System.currentTimeMillis();
        }
    }

    private void doRun() 
    throws CannotBacktrackException, CannotManageStateException, 
    ClasspathException, ThreadStackEmptyException, 
    ContradictionException, DecisionException, EngineStuckException, 
    FailureException, NonexistingObservedVariablesException  {
        if (this.actions.atStart()) { return; }
        //performs the symbolic execution loop
        while (true) {
            if (this.actions.atPathStart()) { return; }

            //explores the path
            while (this.engine.canStep() && currentStateIsInRunSubregion()) {
                if (this.engine.atInitialState()) {
                    if (this.actions.atInitial()) { return; }
                }
                if (this.engine.currentMethodChanged()) {
                    if (this.actions.atMethodPre()) { return; }
                }
                if (this.engine.sourceRowChanged()) {
                    if (this.actions.atSourceRowPre()) { return; }
                }
                if (this.actions.atStepPre()) { return; }
                BranchPoint bp = null;
                try {
                    bp = this.engine.step();
                } catch (CannotManageStateException e) {
                    if (this.actions.atCannotManageStateException(e)) { return; }
                } catch (ClasspathException e) {
                    if (this.actions.atClasspathException(e)) { return; }
                } catch (ContradictionException e) {
                    if (this.actions.atContradictionException(e)) { return; }
                } catch (DecisionException e) {
                    if (this.actions.atDecisionException(e)) { return; }
                } catch (EngineStuckException e) {
                    if (this.actions.atEngineStuckException(e)) { return; }
                } catch (FailureException e) {
                    if (this.actions.atFailureException(e)) { return; }
                } catch (ThreadStackEmptyException e) {
                    if (this.actions.atThreadStackEmptyException(e)) { return; }
                } catch (NonexistingObservedVariablesException e) {
                    if (this.actions.atNonexistingObservedVariablesException(e)) { return; }
                } finally {
                    if (this.actions.atStepFinally()) { return; }
                }
                if (this.actions.atStepPost()) { return; }
                
                if (bp != null) {
                    if (!currentStateIsInRunSubregion()) { break; }
                    if (this.actions.atBranch(bp)) { return; }
                }

                if (outOfScope()) {
                    ++this.pathsOutOfScope; 
                    this.engine.stopCurrentPath();
                    if (outOfScopeHeap()) { 
                        if (this.actions.atScopeExhaustionHeap()) { return; }
                    }
                    if (outOfScopeDepth()) {
                        if (this.actions.atScopeExhaustionDepth()) { return; }
                    }
                    if (outOfScopeCount()) {
                        if (this.actions.atScopeExhaustionCount()) { return; }
                    }
                    if (outOfScopeStack()) {
                        if (this.actions.atScopeExhaustionStack()) { return; }
                    }
                    if (outOfScopeLoops()) {
                        if (this.actions.atScopeExhaustionLoops()) { return; }
                    }
                }

                if (this.timeout > 0) {
                    if (System.currentTimeMillis() - this.startTime > this.timeout) {
                        this.actions.atTimeout();
                        return;
                    }
                }

                if (this.engine.sourceRowChanged() || this.engine.atFrameChanger()) {
                    if (this.actions.atSourceRowPost()) { return; }
                }
                if (this.engine.currentMethodChanged() || this.engine.atFrameChanger()) {
                    if (this.actions.atMethodPost()) { return; }
                }

            }

            //stuck or out-of-run-subregion state reached
            if (currentStateIsInRunSubregion()) {
                //in this case, the state must be stuck (it should be impossible that a state
                //is both stuck and out of the run subregion)
                ++this.pathsTot;
                if (this.actions.atPathEnd()) { return; }
            }

            //backtracks
            if (this.engine.canBacktrack()) {
                if (this.actions.atBacktrackPre()) { return; }

                BranchPoint bp = null;
                boolean found = false;
                try {
                    do {
                        bp = this.engine.backtrack();
                        found = currentStateIsInRunSubregion();
                    } while (!found && this.engine.canBacktrack());
                } catch (DecisionBacktrackException e) {
                    if (this.actions.atDecisionBacktrackException(e)) { return; }
                } catch (CannotBacktrackException e) {
                    if (this.actions.atCannotBacktrackException(e)) { return; }
                } finally {
                    if (this.actions.atBacktrackFinally()) { return; }
                }
                if (found) {
                    if (this.actions.atBacktrackPost(bp)) { return; }
                } else {
                    this.actions.atEnd();
                    return;
                }
            } else {
                this.actions.atEnd();
                return;
            }
        }
    }

    /**
     * Returns the start time, i.e., the time when
     * the method {@link #run()} was invoked.
     * 
     * @return a {@code long}, {@code 0L} if this 
     * method is invoked before {@link #run()}.
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Returns the start time, i.e., the time when
     * the method {@link #run()} was invoked.
     * 
     * @return a {@code long}, {@code 0L} if this 
     * method is invoked before {@link #run()}.
     */
    public long getStopTime() {
        return this.stopTime;
    }

    /**
     * Returns the total number of paths explored until 
     * its invocation.
     * 
     * @return a {@code long}.
     */
    public long getPathsTotal() {
        return this.pathsTot;
    }

    /**
     * Returns the total number of out-of-scope paths explored 
     * until its invocation.
     * 
     * @return a {@code long}.
     */
    public long getPathsOutOfScope() {
        return this.pathsOutOfScope;
    }
}

