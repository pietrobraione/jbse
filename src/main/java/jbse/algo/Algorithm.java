package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.Collection;
import java.util.SortedSet;
import java.util.function.Supplier;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative;
import jbse.val.exc.InvalidTypeException;

/**
 * Interface for all the Strategies for executing a bytecode.
 * 
 * @author Pietro Braione
 *
 */
public abstract class Algorithm<
D extends BytecodeData, 
R extends DecisionAlternative, 
DE extends StrategyDecide<R>, 
RE extends StrategyRefine<R>, 
UP extends StrategyUpdate<R>> {
    /**
     * The number of operands in the operand stack
     * consumed by the bytecode.
     * 
     * @return a {@link Supplier}{@code <}{@link Integer}{@code >}
     *         that, when evaluated, returns the number of 
     *         operands in the operand stack consumed by
     *         the bytecode.
     */
    protected abstract Supplier<Integer> numOperands();

    /** 
     * The bytecode data.
     * 
     * @return a {@link Supplier}{@code <D extends }{@link BytecodeData}{@code >}
     *         that, when evaluated, returns the 
     *         {@link BytecodeData} object that this algorithm 
     *         must use.
     */
    protected abstract Supplier<D> bytecodeData();

    /** 
     * The cooker.
     * 
     * @return The {@link BytecodeCooker}
     *         object that this algorithm must use.
     */
    protected abstract BytecodeCooker bytecodeCooker();

    /** 
     * Reifies {@code R}.
     * 
     * @return the (super)class of the {@link DecisionAlternative}
     *         object that this algorithm uses.
     */
    protected abstract Class<R> classDecisionAlternative();
    

    /** 
     * The decider.
     * 
     * @return The {@link StrategyDecide}
     *         object that this algorithm must use.
     */
    protected abstract DE decider();
    

    /** 
     * The refiner.
     * 
     * @return The {@link StrategyRefine}
     *         object that this algorithm must use.
     */
    protected abstract RE refiner();
    

    /** 
     * The updater.
     * 
     * @return The {@link BytecodeCooker}
     *         object that this algorithm must use.
     */
    protected abstract UP updater();
    
    /**
     * How the value returned by {@link #programCounterUpdate()} 
     * should be interpreted.
     * 
     * @return a {@link Supplier}{@code <}{@link Boolean}{@code >} returning
     *         {@code true} if {@link #programCounterUpdate()} 
     *         returns an offset from the state's current program
     *         counter, {@code false} if {@link #programCounterUpdate()}
     *         returns a new, absolute program counter.
     */
    protected abstract Supplier<Boolean> isProgramCounterUpdateAnOffset();
    
    /**
     * Either the program counter offset or the 
     * program counter value, after the execution 
     * of the bytecode.
     * 
     * @return a {@link Supplier}{@code <}{@link Integer}{@code >}.
     */
    protected abstract Supplier<Integer> programCounterUpdate();
    
    /**
     * Override this method to perform cleanup of 
     * the algorithm's state whenever it has some state. 
     * Cleanup will be performed at the beginning of {@link #exec} 
     * to set the {@link Algorithm} to its virgin state.
     */
    protected void cleanup() { }
    
    private final Supplier<Integer> numOperands; //just caches
    protected D data; //just caches
    private final BytecodeCooker cooker;  //just caches
    private final DE decider; //just caches
    private final RE refiner; //just caches
    private final UP updater; //just caches
    protected final Supplier<Integer> programCounterUpdate; //just caches
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset; //just caches
    
    public Algorithm() {
        this.numOperands = numOperands();
        this.data = null; //to be initialized lazily
        this.cooker = bytecodeCooker();
        this.decider = decider();
        this.refiner = refiner();
        this.updater = updater();
        this.programCounterUpdate = programCounterUpdate();
        this.isProgramCounterUpdateAnOffset = isProgramCounterUpdateAnOffset();
    }
    
    /**
     * What to do in case of {@link InvalidInputException}.
     * By default the execution fails
     * with the {@link InvalidInputException} as cause.
     * 
     * @param state the {@link State}.
     * @param e the {@link InvalidInputException} thrown.
     */
    protected void onInvalidInputException(State state, InvalidInputException e) {
        failExecution(e);
    }

    /**
     * What to do in case of {@link BadClassFileException}.
     * By default the execution fails
     * with the {@link BadClassFileException} as cause.
     * 
     * @param state the {@link State}.
     * @param e the {@link BadClassFileException} thrown.
     */
    protected void onBadClassFileException(State state, BadClassFileException e) {
        failExecution(e);
    }
    
    /** 
     * Checks whether some reference was not 
     * expanded by resolution during {@link #exec}.
     * 
     * @return {@code true} if some reference was 
     * resolved but not expanded, {@code false}
     * otherwise (i.e., no reference was resolved
     * or all the resolved references were expanded).
     */
    public boolean someReferenceNotExpanded() { 
        return false; 
    }
    
    //TODO improve the two methods that follow (possibly return a java.util.List of the References)

    /**
     * Returns a list of the origins of the nonexpanded
     * references origins.
     * 
     * @return a {@link String}.
     */
    public String nonExpandedReferencesOrigins() { 
        return null; 
    }
    
    /**
     * Returns a list of the origins of the nonexpanded
     * references types.
     * 
     * @return a {@link String}.
     */
    public String nonExpandedReferencesTypes() { 
        return null; 
    }
    
    protected ExecutionContext ctx; //just caches across a call of exec (note that this makes Algorithms nonreentrant!)

    /**
     * Executes the algorithm.
     * 
     * @param state the {@link State} on which the algorithm must operate.
     * @param ctx the {@link ExecutionContext}.
     * @throws DecisionException
     * @throws ContradictionException
     * @throws ThreadStackEmptyException
     * @throws ClasspathException
     * @throws CannotManageStateException
     * @throws FailureException
     * @throws ContinuationException
     */
    public final void exec(State state, ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException, ClasspathException, 
    CannotManageStateException, FailureException, 
    ContinuationException {
        cleanup();
        this.ctx = ctx;
        try {
            doExec(state);
        } catch (InvalidInputException e) {
            onInvalidInputException(state, e);
        } catch (BadClassFileException e) {
            onBadClassFileException(state, e);
        }
    }

    private void doExec(State state) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException, ClasspathException, 
    InvalidInputException, BadClassFileException, 
    CannotManageStateException, FailureException, 
    ContinuationException {
        if (this.data == null) {
            this.data = bytecodeData().get();
        }
        try {
            this.data.read(state, this.numOperands);
            this.cooker.cook(state);
        } catch (InterruptException e) {
            if (e.hasContinuation()) {
                throw new ContinuationException(e.getContinuation());
            } else {
                return;
            }
        }

        //decides the satisfiability of the different alternatives
        final SortedSet<R> decisionResults = this.ctx.mkDecisionResultSet(classDecisionAlternative());     
        final Outcome outcome = this.decider.decide(state, decisionResults);

        //checks if at least one alternative is satisfiable
        final int tot = decisionResults.size();
        if (tot == 0) {
            throw new ContradictionException();
        }

        //generates the next states
        final boolean shouldRefine = outcome.shouldRefine();
        final boolean branchingDecision = outcome.branchingDecision();
        final boolean branchAdded = possiblyAddBranchPoint(decisionResults);
        int cur = 1;
        for (R result : decisionResults) {
            final State stateCurrent = (cur < tot ? state.clone() : state);

            //pops the operands from the operand stack
            try {
                stateCurrent.popOperands(this.numOperands.get());
            } catch (InvalidNumberOfOperandsException e) {
                failExecution(e);
            }

            //possibly refines the state
            try {
                if (shouldRefine) {
                    this.refiner.refine(stateCurrent, result);
                }
            } catch (InvalidTypeException e) {
                failExecution(e);
            }

            //completes the bytecode semantics
            InterruptException interrupt = null;
            try {
                this.updater.update(stateCurrent, result);
            } catch (InterruptException e) {
                interrupt = e;
            } catch (ThreadStackEmptyException e) {
                failExecution(e);
            }

            //updates the program counter
            try {
                if (stateCurrent.isStuck()) {
                    //nothing to do
                } else if (interrupt == null) {
                    if (this.isProgramCounterUpdateAnOffset.get()) {
                        stateCurrent.incProgramCounter(this.programCounterUpdate.get());
                    } else {
                        stateCurrent.setProgramCounter(this.programCounterUpdate.get());
                    }
                } else if (interrupt.hasContinuation()) {
                    throw new ContinuationException(interrupt.getContinuation());
                } //else, nothing to do
            } catch (InvalidProgramCounterException e) {
                throwVerifyError(stateCurrent);
            }

            //is the state the result of a branching decision?
            stateCurrent.setBranchingDecision(branchingDecision);

            //adds the created state to the tree, if on a new branch
            if (branchAdded) {
                this.ctx.stateTree.addState(stateCurrent, result.getBranchNumber(), result.getIdentifier());
            }

            ++cur;
        }
    }
    
    private boolean possiblyAddBranchPoint(Collection<R> decisionResults) {
        final boolean moreThanOneResult = (decisionResults.size() > 1);
        final DecisionAlternative d = decisionResults.iterator().next();
        final boolean trivial = d.trivial();
        final boolean concrete = d.concrete();
        final boolean noDecision = d.noDecision();
        return this.ctx.stateTree.possiblyAddBranchPoint(moreThanOneResult, trivial, concrete, noDecision);
    }
}
