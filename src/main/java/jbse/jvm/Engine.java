package jbse.jvm;

import static jbse.bc.Opcodes.OP_ARETURN;
import static jbse.bc.Opcodes.OP_ATHROW;
import static jbse.bc.Opcodes.OP_DRETURN;
import static jbse.bc.Opcodes.OP_FRETURN;
import static jbse.bc.Opcodes.OP_INVOKEINTERFACE;
import static jbse.bc.Opcodes.OP_INVOKESPECIAL;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;
import static jbse.bc.Opcodes.OP_INVOKEVIRTUAL;
import static jbse.bc.Opcodes.OP_IRETURN;
import static jbse.bc.Opcodes.OP_RETURN;

import java.util.Collection;
import java.util.List;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.InterruptException;
import jbse.algo.Action;
import jbse.algo.Action_START;
import jbse.algo.exc.CannotManageStateException;
import jbse.apps.run.DecisionProcedureGuidance;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionBacktrackException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.Clause;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.StateTree.BranchPoint;
import jbse.val.ReferenceSymbolic;

/**
 * An {@code Engine} is a JVM able to symbolically execute the 
 * methods of an arbitrary Java class. 
 * An {@code Engine} can be integrated with verification and testing 
 * tools by means of the following features:
 * 
 * <ul>
 * <li>Stepwise symbolic execution of methods of Java classes;</li>
 * <li>Automatic detection of feasible branches at decision points, predictable 
 * exploration and possibility to backtrack the execution to
 * unexplored branches;</li>
 * <li>Possibility of hooking observers on state variable changes;</li>
 * <li>Support to assumptions (to prune irrelevant executions) 
 * and assertions (to detect error states);</li>
 * <li>Support to guided symbolic execution, where a more concrete 
 * execution guides a less concrete one through a set of paths.</li>
 * </ul>
 * 
 * @author Pietro Braione
 * @author unknown
 */
public final class Engine implements AutoCloseable {
    //Architecture of the engine

    /** The {@link ExecutionContext}. */
    private final ExecutionContext ctx; 

    /** The {@link VariableObserverManager}. */
    private final VariableObserverManager vom;

    //State of the execution

    /** The current JVM {@link State} of the symbolic execution. */
    private State currentState;

    /** 
     * Whether some of the references resolved by the last
     * decision procedure call has not been expanded.
     */
    private boolean someReferencePartiallyResolved;

    /**
     * List of the references partially resolved by the 
     * last decision procedure call.
     */
    private List<ReferenceSymbolic> partiallyResolvedReferences;

    /** 
     * Stores the source row of the bytecode executed before the current one 
     * (-1 if none has before, or no debug information was available).
     */
    private int preStepSourceRow = -1;

    /**
     * Stores the size of the current state's stack before a step; used as 
     * a quick and dirty way for understanding whether a step caused a change
     * in current method.
     */
    private int preStepStackSize;

    //Execution statistics

    /** The total number of {@link State}s analyzed by the {@link Engine}. */
    private long analyzedStates = 0L;
    
    //Construction.

    /**
     * Constructor. Used by the builder.
     * 
     * @param ctx an {@link ExecutionContext}.
     */
    Engine(ExecutionContext ctx, VariableObserverManager vom) {
        this.ctx = ctx;
        this.vom = vom;
    }

    /**
     * Steps the engine in a suitable start state from which execution can be continued
     * by invoking {@link #step()}. Such a state is either the one stored in 
     * {@code this.ctx} or a <em>pre-initial</em> state that bootstraps the standard
     * java classes before the symbolic execution of the target method. In the
     * pre-initial state:
     * 
     * <ul>
     * <li> the path condition is set to {@code true};</li>
     * <li> the heap is empty;</li>
     * <li> the stack has many frames loaded for the execution 
     * of several methods that initialize the virtual machine.</li>
     * </ul>
     * 
     * @throws DecisionException in case initialization of the 
     *         decision procedure fails for some reason.
     * @throws InitializationException in case this {@link Engine} is not
     *         executed on a Java 8 runtime environment, or some error happened
     *         while initializing some indispensable Java standard classes, 
     *         or while creating the initial thread or thread group. 
     * @throws InvalidClassFileFactoryClassException in case the class object 
     *         provided to build a class file factory cannot be used
     *         (e.g., it has not a suitable constructor or it is not visible).
     * @throws NonexistingObservedVariablesException in case some of the provided 
     *         observed variable names cannot be observed. This is the only exception
     *         that allows nevertheless to perform symbolic execution, in which case 
     *         only the observers to existing variables will be notified.
     * @throws ClasspathException in case any of the indispensable standard Java 8 
     *         Runtime Environment classes is missing from the bootstrap classpath, 
     *         or is ill-formed.
     * @throws ContradictionException if some initialization assumption is
     *         contradicted.
     */
    void init() 
    throws DecisionException, InitializationException, 
    InvalidClassFileFactoryClassException, NonexistingObservedVariablesException, 
    ClasspathException, ContradictionException {
    	try {
    		//checks that the version of the meta-level JRE is ok
    		final String javaVersion = System.getProperty("java.version");
    		if (!javaVersion.startsWith("1.8.0")) {
    			throw new InitializationException("JBSE requires to be run on a Java 8 JVM.");
    		}

    		//steps
    		final Action_START algo = this.ctx.dispatcher.selectStart();
    		algo.exec(this.ctx);
    		
    		//updates the current state
    		if (this.ctx.stateTree.createdBranch()) { //Action_START always creates a branch, but we need the side effect of invoking createBranch
    			this.currentState = this.ctx.stateTree.nextState();
    		} else {
    			//this should never happen
    			throw new UnexpectedInternalException("The first state is missing from the state tree.");
    		}

    		//in the case the state is initial does some operations
    		if (atInitialState()) {
    			this.ctx.switchInitial(this.currentState);
        		this.vom.init(this);
    		}

    		//synchronizes the decision procedure with the current path condition
            final Collection<Clause> currentAssumptions = this.currentState.getPathCondition();
    		this.ctx.decisionProcedure.setAssumptions(currentAssumptions);
    		this.currentState.resetLastPathConditionClauses();
    		
    		//if the decision procedure is a guidance decision procedure,
    		//set the execution context
    		if (this.ctx.decisionProcedure instanceof DecisionProcedureGuidance) {
    			((DecisionProcedureGuidance) this.ctx.decisionProcedure).setExecutionContext(this.ctx);
    		}
        } catch (InvalidInputException | ThreadStackEmptyException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    //public methods (operations)
    
    /**
     * Returns this {@link Engine}'s {@link ExecutionContext}.
     * 
     * @return an {@link ExecutionContext}.
     */
    public ExecutionContext getExecutionContext() {
    	return this.ctx;
    }
    
    /** 
     * Checks whether the current state is the last
     * pre-initial state, i.e., the last state before 
     * the start of the symbolic execution of the 
     * target method.
     * 
     * @return a {@code boolean}.
     */
    public boolean atLastPreInitialState() {
        return (this.currentState.phase() == Phase.PRE_INITIAL && this.currentState.getStackSize() == 0);
    }
    
    /** 
     * Checks whether the current state is the initial
     * state, i.e., the first state after the pre-initial phase.
     * 
     * @return a {@code boolean}.
     */
    public boolean atInitialState() {
        return (this.currentState.phase() == Phase.INITIAL);
    }

    /**
     * Returns the initial state (a safety copy).
     * 
     * @return the initial {@link State}, not refined on the current
     *         path condition, or {@code null} if we are in the
     *         pre-initial phase.
     */
    public State getInitialState() {
        return this.ctx.getStateInitial();
    }

    /**
     * Checks whether the engine can step.
     * 
     * @return {@code true} iff the engine can step. The engine can step unless 
     *         the current {@link State} is stuck, or no backtrack has 
     *         been performed since the last invocation of the {@link #stopCurrentPath}
     *         method.
     */
    public boolean canStep() {
        return !(this.currentState.isStuck());
    }

    /**
     * Steps the execution.
     * 
     * @return the {@link BranchPoint} created after the execution of the 
     *         current bytecode, allowing to resume the execution from the states 
     *         produced by it, or {@code null} if the bytecode execution 
     *         does not produce more than one possible next state.
     * @throws CannotManageStateException iff the engine is unable to calculate 
     *         the next state because of some engine limitations.
     * @throws NonexistingObservedVariablesException in case some of the provided 
     *         observed variable names cannot be observed. This is the only exception
     *         that allows nevertheless to perform symbolic execution, in which case 
     *         only the observers to existing variables will be notified.
     * @throws ClasspathException in case any of the indispensable standard Java 8 
     *         Runtime Environment classes is missing from the bootstrap classpath, 
     *         or is ill-formed.
     * @throws ContradictionException iff the step causes a violation of some assumption; 
     *         in this case after the step it is 
     *         {@link #canStep() canStep}{@code () == false}.
     * @throws DecisionException iff the decision procedure fails for any reason.
     * @throws EngineStuckException when the method is invoked from a state where 
     *         {@link #canStep() canStep}{@code () == false}.
     * @throws ThreadStackEmptyException when the execution of a step is attempted
     *         on a current state with an empty thread stack.
     * @throws FailureException iff the step causes a violation of some assertion; 
     *         in this case after the step it is 
     *         {@link #canStep() canStep}{@code () == false}.
     */
    public BranchPoint step() 
    throws EngineStuckException, CannotManageStateException, NonexistingObservedVariablesException, 
    ClasspathException, ThreadStackEmptyException, ContradictionException, DecisionException, 
    FailureException {
        try {
        	//checks the precondition
        	if (!canStep()) {
        		throw new EngineStuckException();
        	}

        	//sets the next phase of the state
        	final boolean atLastPreInitialState = atLastPreInitialState(); //safety copy
        	if (atLastPreInitialState) {
        		this.currentState.setPhaseInitial();
        	} else if (atInitialState()) {
        		this.currentState.setPhasePostInitial();
        	}

        	//updates the information about the state before the step
        	this.preStepStackSize = this.currentState.getStackSize();
        	this.preStepSourceRow = (this.preStepStackSize == 0 ? -1 : this.currentState.getSourceRow());

        	//steps
        	Action action = (atLastPreInitialState ? 
  				             this.ctx.dispatcher.selectInit() :
  				             this.ctx.dispatcher.select(this.currentState.getInstruction()));
        	boolean hasContinuation;
        	do {
        		try {
        			action.exec(this.currentState, this.ctx);
        			hasContinuation = false;
        		} catch (InterruptException e) {
        			hasContinuation = e.hasContinuation();
        			if (hasContinuation) {
        				action = e.getContinuation();
        			}
        		} catch (ClasspathException | CannotManageStateException | 
        				ThreadStackEmptyException | ContradictionException | 
        				DecisionException | FailureException | 
        				UnexpectedInternalException e) {
        			stopCurrentPath();
        			throw e;
        		} 
        	} while (hasContinuation);

        	//possibly gets information about symbolic references that were not expanded
        	if (action instanceof Algorithm<?, ?, ?, ?, ?>) {
        		final Algorithm<?, ?, ?, ?, ?> algo = (Algorithm<?, ?, ?, ?, ?>) action;
        		this.someReferencePartiallyResolved = algo.someReferencePartiallyResolved();
        		this.partiallyResolvedReferences = algo.partiallyResolvedReferences();
        	}
        	
        	//cleans, stores and creates a branch for the initial state
    	    if (atInitialState()) {
    			this.currentState.gc();
    			this.ctx.switchInitial(this.currentState);
        		this.vom.init(this);
    	    	this.ctx.stateTree.addStateInitial(this.currentState);
    	    }

        	//updates the current state and calculates the return value
        	BranchPoint retVal = null;
        	if (this.ctx.stateTree.createdBranch()) {
        		retVal = this.ctx.stateTree.nextBranch();
        		this.currentState = this.ctx.stateTree.nextState();
        	} else {
        		this.currentState.incSequenceNumber();
        	}
        	
        	//updates the counters for depth/count scope
        	if (this.currentState.branchingDecision() && !this.currentState.stutters()) {
        		this.currentState.incDepth();
        		this.currentState.resetCount();
        	} else {
        		this.currentState.incCount();
        	}

        	//synchronizes the decision procedure with the current path condition
        	if (this.currentState.areThereNewPathConditionClauses()) {
        		this.ctx.decisionProcedure.addAssumptions(this.currentState.getLastPathConditionPushedClauses());
        		this.currentState.resetLastPathConditionClauses();
        	}

        	//notifies observers of variables
        	if (this.currentState.phase() == Phase.POST_INITIAL) {
        		this.vom.notifyObservers(retVal);
        	}

        	//updates stats
        	if (this.analyzedStates < Long.MAX_VALUE) { 
        		++this.analyzedStates;
        	}

        	//returns
        	return retVal;
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Returns the engine's current JVM state 
     * (<em>not</em> a copy).
     * 
     * @return a {@link State}.
     */
    public State getCurrentState() {
        return this.currentState;
    }

    /**
     * Returns the number of analyzed symbolic states.
     * 
     * @return a {@code long}, the number of times the 
     *         {@link #step()} method has been invoked.  
     */
    public long getAnalyzedStates() {
        return this.analyzedStates;
    }

    /**
     * Returns the number of assumed object of a given class.
     * 
     * @param className a {@link String}.
     * @return the number of objects with class {@code className}
     * assumed in the current state.
     */
    public int getNumAssumed(String className) {
        return this.currentState.getNumAssumed(className);
    }
    
    /**
     * Returns the number of states that remain to be explored
     * at a given branch.
     * 
     * @param bp a {@link BranchPoint}.
     * @return the number of states at the branch identified by {@code bp} 
     *         that were not yet analyzed.
     */
    public int getNumOfStatesAtBranch(BranchPoint bp) {
    	return this.ctx.stateTree.getNumOfStatesAtBranch(bp);
    }

    /**
     * Returns a state at a given branch.
     * 
     * @param bp a {@link BranchPoint}.
     * @param index an {@code int}.
     * @return the state at the branch identified by {@code bp} 
     *         with position {@code index} in the branch.
     * @throws InvalidInputException if 
     *         {@code index < 0 || index > }{@link #getNumOfStatesAtBranch(BranchPoint) getNumOfStatesAtBranch}{@code (bp)}. 
     */
    public State getStateAtBranch(BranchPoint bp, int index) throws InvalidInputException {
    	return this.ctx.stateTree.getStateAtBranch(bp, index);
    }

    /**
     * Stops the execution along the current path.
     */
    public void stopCurrentPath() {
    	try {
    		this.currentState.setStuckStop();
		} catch (FrozenStateException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Checks whether the engine can backtrack to some state.
     * 
     * @return {@code true} iff the engine has at least one 
     *         pending backtrack point.
     */
    public boolean canBacktrack() {
        return this.ctx.stateTree.hasStates();
    }

    /**
     * Backtracks the execution to the next pending branch.
     * 
     * @return the {@link BranchPoint} of the next pending branch.
     * @throws CannotBacktrackException iff {@link #canBacktrack}{@code () == false} 
     *         before the method is invoked.
     * @throws DecisionBacktrackException iff the decision procedure fails for 
     *         any reason. 
     */
    public BranchPoint backtrack() 
    throws CannotBacktrackException, DecisionBacktrackException {
        //TODO dubious correctness of this implementation
        if (!canBacktrack()) {
            throw new CannotBacktrackException();
        }

        final BranchPoint bp = this.ctx.stateTree.nextBranch();
        final boolean isLast = (getNumOfStatesAtBranch(bp) == 1);

        try {
            this.currentState = this.ctx.stateTree.nextState();
            final Collection<Clause> currentAssumptions = this.currentState.getPathCondition();
            this.ctx.decisionProcedure.setAssumptions(currentAssumptions);
            this.currentState.resetLastPathConditionClauses();

            //updates the counters for depth/count scope
            if (this.currentState.branchingDecision()) {
                this.currentState.incDepth();
                this.currentState.resetCount();
            } else {
                this.currentState.incCount();
            }
        } catch (DecisionException e) {
            throw new DecisionBacktrackException(e);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }

        this.vom.restoreObservedVariablesValues(bp, isLast);

        return bp;
    }

    /**
     * Tests whether some of the references resolved by the last
     * decision procedure call were <em>partially</em>, resolved, 
     * i.e., were not resolved by expansion because no 
     * concrete class was found in the classpath that is 
     * type-compatible with the symbolic references and assumed to 
     * be not pre-initialized.
     * 
     * @return a {@code boolean}.
     */
    public boolean someReferencePartiallyResolved() {
        return this.someReferencePartiallyResolved;
    }

    /**
     * Returns a list of the symbolic references resolved by the last
     * decision procedure call that were <em>partially</em>, resolved.

     * @return a {@link List}{@code <}{@link ReferenceSymbolic}{@code >}, 
     *         empty when {@link #someReferencePartiallyResolved()}{@code  == false}.
     * @see #someReferencePartiallyResolved()
     */
    public List<ReferenceSymbolic> getPartiallyResolvedReferences() {
        return this.partiallyResolvedReferences;
    }

    /**
     * Checks whether the current bytecode is in a different source row
     * than the previous one.
     * 
     * @return {@code true} iff the current bytecode is in a source code 
     *         statement different from the previous one, or the current 
     *         state is stuck.
     * @throws ThreadStackEmptyException if the thread stack is empty.
     */
    public boolean sourceRowChanged() throws ThreadStackEmptyException {
        try {
			return (this.currentState.isStuck() || this.currentState.getStackSize() == 0 ||
			        this.currentState.getSourceRow() != this.preStepSourceRow);
		} catch (FrozenStateException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Checks whether the current method has changed.
     * 
     * @return {@code true} iff the current frame is different
     *         from that previous the execution of a bytecode, 
     *         or the current state is stuck.
     */
    public boolean currentMethodChanged() {
			return (this.currentState.isStuck() || 
			        this.currentState.getStackSize() != this.preStepStackSize);
    }

    /**
     * Checks whether the next bytecode may change the current frame.
     * 
     * @return {@code true} iff the next instruction is one of invoke*, 
     * athrow or *return, or the current state is stuck.
     * @throws ThreadStackEmptyException 
     */
    public boolean atFrameChanger() throws ThreadStackEmptyException {
        if (this.currentState.isStuck()) {
            return true;
        }
        try {
        	final byte currentInstruction = this.currentState.getInstruction(); 
        	return (currentInstruction == OP_INVOKEVIRTUAL ||
        			currentInstruction == OP_INVOKESPECIAL ||
        			currentInstruction == OP_INVOKESTATIC ||
        			currentInstruction == OP_INVOKEINTERFACE ||
        			currentInstruction == OP_IRETURN ||
        			currentInstruction == OP_FRETURN ||
        			currentInstruction == OP_DRETURN ||
        			currentInstruction == OP_ARETURN ||
        			currentInstruction == OP_RETURN ||
        			currentInstruction == OP_ATHROW);
		} catch (FrozenStateException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Cleans up the decision procedure after the usage of the engine.
     * 
     * @throws DecisionException when clean-up of decision procedure fails.
     */
    @Override
    public void close() throws DecisionException {
        this.ctx.decisionProcedure.close();
    }
}