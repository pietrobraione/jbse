package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Supplier;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract class for all the Strategies for executing a symbolic
 * execution step.
 * 
 * @author Pietro Braione
 */
public abstract class Algorithm<
D extends BytecodeData, 
R extends DecisionAlternative, 
DE extends StrategyDecide<R>, 
RE extends StrategyRefine<R>, 
UP extends StrategyUpdate<R>> implements Action {
    /**
     * The number of operands in the operand stack
     * consumed by the {@link Algorithm}.
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
     * @return The {@link StrategyUpdate}
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
    private UP updater; //just caches
    protected final Supplier<Integer> programCounterUpdate; //just caches
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset; //just caches

    public Algorithm() {
        this.numOperands = numOperands();
        this.data = null; //to be initialized lazily
        this.cooker = bytecodeCooker();
        this.decider = decider();
        this.refiner = refiner();
        this.updater = null; //to be initialized lazily (at construction time no ExecutionContext is available)
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
     * @throws DecisionException possibly raised if the action uses a 
     *         decision procedure and the decision procedure fails.
     * @throws ContradictionException possibly raised if the action execution
     *         results in no successor states, which happens whenever all the 
     *         candidate successors (and thus {@code state}) fail to satisfy 
     *         the execution assumptions. 
     * @throws ClasspathException possibly raised if some core 
     *         standard class is missing from the classpath of ill-formed.
     * @throws CannotManageStateException possibly raised if the 
     *         action cannot be executed due to limitations of JBSE.
     * @throws FailureException possibly raised to signal the violation
     *         of an assertion.
     * @throws ContinuationException if the execution of this action must
     *         be interrupted, and possibly followed by the execution of another
     *         action.
     */
    protected void onInvalidInputException(State state, InvalidInputException e) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException, ClasspathException, 
    CannotManageStateException, FailureException, 
    InterruptException {
        failExecution(e);
    }

    /** 
     * Checks whether some reference was partially
     * resolved during {@link #exec}.
     * 
     * @return {@code true} if some reference was 
     * partially resolved, {@code false}
     * otherwise (i.e., all the references were 
     * either not resolved or resolved fully).
     */
    public boolean someReferencePartiallyResolved() { 
        return false; //default implementation
    }

    /**
     * Returns a {@link List} of the partially resolved 
     * references.
     * 
     * @return a {@link List}{@code <}{@link ReferenceSymbolic}{@code >}.
     */
    public List<ReferenceSymbolic> partiallyResolvedReferences() { 
        return Collections.emptyList(); //default implementation
    }

    protected ExecutionContext ctx; //just caches across a call of exec (note that this makes Algorithms nonreentrant!)

    @Override
    public final void exec(State state, ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException, ClasspathException, 
    CannotManageStateException, FailureException, 
    InterruptException {
        cleanup();
        this.ctx = ctx;
        try {
            doExec(state);
        } catch (InvalidInputException e) {
            onInvalidInputException(state, e);
        }
    }

    private void doExec(State state) 
    throws DecisionException, ContradictionException, 
    ClasspathException, InvalidInputException, 
    CannotManageStateException, FailureException, 
    InterruptException {
    	//initializes lazily this.data
        if (this.data == null) {
            this.data = bytecodeData().get();
        }
        
        try {
            this.data.read(state, this.ctx.getCalculator(), this.numOperands);
            this.cooker.cook(state);
        } catch (InterruptException e) {
        	state.setStutters(true);
        	throw e;
        } catch (InvalidTypeException | InvalidOperatorException | 
        		 InvalidOperandException | ThreadStackEmptyException | 
        		 RenameUnsupportedException | InvalidProgramCounterException | 
        		 FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }

        //decides the satisfiability of the different alternatives
        final SortedSet<R> decisionResults = this.ctx.mkDecisionResultSet(classDecisionAlternative());
        final Outcome outcome;
        try {
        	outcome = this.decider.decide(state, decisionResults);
        } catch (InterruptException e) {
        	state.setStutters(true);
        	throw e;
        }
        
        //checks if at least one alternative is satisfiable
        final int tot = decisionResults.size();
        if (tot == 0) {
            throw new ContradictionException();
        }

        //generates the next states
        final boolean shouldRefine = outcome.shouldRefine();
        final boolean branchingDecision = outcome.branchingDecision();
        final boolean branchAdded = possiblyAddBranchPoint(decisionResults);
        for (R result : decisionResults) {
            final State stateCurrent = (tot > 1 ? state.lazyClone() : state);

            InterruptException interrupt = null;
            try {
                //possibly refines the state
                if (shouldRefine) {
                    this.refiner.refine(stateCurrent, result);
                }

                //pops the operands from the operand stack
            	stateCurrent.popOperands(this.numOperands.get());

            	//initializes lazily this.updated
                if (this.updater == null) {
                    this.updater = updater();
                }

                //completes the bytecode semantics
                this.updater.update(stateCurrent, result);
            } catch (InterruptException e) {
                interrupt = e;
            } catch (InvalidInputException | InvalidTypeException | 
                     InvalidOperatorException | InvalidOperandException | 
                     ThreadStackEmptyException | InvalidNumberOfOperandsException e) {
                //this should never happen
                failExecution(e);
            }

            //updates the program counter
            try {
                if (stateCurrent.isStuck() || stateCurrent.getStackSize() == 0) {
                	stateCurrent.setStutters(false);
                } else if (interrupt == null) {
                    if (this.isProgramCounterUpdateAnOffset.get()) {
                        stateCurrent.incProgramCounter(this.programCounterUpdate.get());
                    } else {
                        stateCurrent.setProgramCounter(this.programCounterUpdate.get());
                    }
                    stateCurrent.setStutters(false);
                } else if (interrupt.hasContinuation()) {
                    //this should never happen
                    failExecution("Thrown an InterruptException with continuation from a refiner or an updater.");
                } else {
                	stateCurrent.setStutters(true);
                }
            } catch (InvalidProgramCounterException e) {
                throwVerifyError(stateCurrent, this.ctx.getCalculator());
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }

            //is the state the result of a branching decision?
            stateCurrent.setBranchingDecision(branchingDecision);

            //adds the created state to the tree, if on a new branch
            if (branchAdded) {
                this.ctx.stateTree.addState(stateCurrent, result.getBranchNumber(), result.getIdentifier());
            }
        }
        
        if (tot > 1) {
        	state.freeze();
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
