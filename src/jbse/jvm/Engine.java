package jbse.jvm;

import java.util.Collection;

import jbse.algo.Algorithm;
import jbse.algo.DispatcherBytecodeAlgorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.Algo_INIT;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.InterruptException;
import jbse.bc.Opcodes;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
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
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.StateTree.BranchPoint;

/**
 * An {@code Engine} is a JVM able to symbolically execute the 
 * methods of an arbitrary Java class. 
 * An {@code Engine} can be integrated with verification and testing 
 * tools by means of the following features:
 * 
 * <ul>
 * <li>Stepwise symbolic execution of public methods of Java classes;</li>
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
public class Engine implements AutoCloseable {
	//Architecture of the engine
	
	/** The engine's {@link DispatcherBytecodeAlgorithm}. */
	private final DispatcherBytecodeAlgorithm dispatcher = new DispatcherBytecodeAlgorithm();

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
	private boolean someReferenceNotExpanded;

	/**
	 * List of the origins of the references 
	 * resolved but not expanded by the last decision 
	 * procedure call.
	 */
	private String nonExpandedReferencesOrigins;

	/**
	 * List of the static types of the references 
	 * resolved but not expanded by the last decision 
	 * procedure call.
	 */
	private String nonExpandedReferencesTypes;

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
	 * Steps the engine in a suitable initial state, either the one stored in 
	 * {@code this.ctx} or the state where:
	 * 
	 * <ul>
	 * <li> the stack has one frame for an invocation of the root method;</li>
	 * <li> if the root method is not static, the heap has one object, an 
	 * instance for the {@code this} parameter of the method; this object 
	 * is the <emph>root object</emph>;</li>
	 * <li> all the local variables in the current frame and all the instance 
	 * variables of the root object (if present) are initialized with symbolic 
	 * values;</li>
	 * <li> the path condition assumes that the class of the root method is
	 * preloaded, and (if the method is not static) that the {@code this}
	 * parameter is resolved by expansion to the root object.</li>
	 * </ul>
	 * 
	 * @throws DecisionException in case initialization of the 
	 *         decision procedure fails for some reason.
	 * @throws InitializationException in case the specified root method 
	 *         does not exist or cannot be symbolically executed for 
	 *         any reason (e.g., is native).
	 * @throws ThreadStackEmptyException 
	 * @throws InvalidClassFileFactoryClassException in case the class object 
	 *         provided to build a class file factory cannot be used
	 *         (e.g., it has not a suitable constructor or it is not visible).
	 * @throws NonexistingObservedVariablesException in case some of the provided 
	 *         observed variable names cannot be observed. This is the only exception
	 *         that allows nevertheless to perform symbolic execution, in which case 
	 *         only the observers to existing variables will be notified.
	 * @throws ClasspathException 
	 */
	void init() 
	throws DecisionException, InitializationException, 
	InvalidClassFileFactoryClassException, NonexistingObservedVariablesException, 
	ClasspathException {
		//executes the initial state setup step
		final Algo_INIT algo = this.dispatcher.select();
		algo.exec(ctx);

		//extracts the initial state from the tree
		this.currentState = this.ctx.stateTree.nextState();
		
        //synchronizes the decision procedure with the path condition
		this.ctx.decisionProcedure.setAssumptions(this.currentState.getPathCondition());
		this.currentState.resetLastPathConditionClauses();

        //inits the variable observer manager
		try {
			this.vom.init(this);
		} catch (ThreadStackEmptyException e) {
			//should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	//TODO eliminate this! (see jbse.meta.algo.SEInvokeAssertRepOk)
	public ExecutionContext getContext() {
		return this.ctx;
	}

	//public methods (operations)
	
	/**
	 * Returns the engine's initial JVM state (a safety copy).
	 * 
	 * @return the initial {@link State} as it was at the beginning
	 *         of the symbolic execution. It is not refined on the current
	 *         path condition.
	 */
	public State getInitialState() {
		return this.ctx.getInitialState();
	}

	/**
	 * Checks whether the engine can step.
	 * 
	 * @return <code>true</code> iff the engine can step. The engine can step unless 
	 *         the current {@link State} is stuck, or no backtrack has 
	 *         been performed since the last invocation of the {@link #stopCurrentTrace}
	 *         method.
	 */
	public boolean canStep() {
		return !(this.currentState.isStuck());
	}

	/**
	 * Steps the execution by performing the current bytecode.
	 * 
	 * @return the {@link BranchPoint} created after the execution of the 
	 *         current bytecode, allowing to resume the execution from the states 
	 *         produced by it, or {@code null} if the bytecode execution 
	 *         does not produce more than one possible next state.
	 * @throws CannotManageStateException iff the engine is unable to calculate 
	 *         the next state because of some engine limitations.
     * @throws ClasspathException iff the JRE standard libraries are missing from
     *         the classpath or incompatible with the current JBSE.
	 * @throws ContradictionException iff the step causes a violation of some assumption; 
	 *         in this case after the step it is 
	 *         {@code this.}{@link #canStep()}{@code == false}.
	 * @throws DecisionException iff the decision procedure fails for any reason.
	 * @throws EngineStuckException when the method is invoked from a state where 
	 *         {@code this.}{@link #canStep()}{@code == false}.
	 * @throws ThreadStackEmptyException when the execution of a step is attempted
	 *         on a current state with an empty thread stack.
	 * @throws FailureException iff the step causes a violation of some assertion; 
	 *         in this case after the step it is 
	 *         {@code this.}{@link #canStep()}{@code == false}.
	 */
	public BranchPoint step() 
	throws EngineStuckException, CannotManageStateException, ClasspathException, 
	ThreadStackEmptyException, ContradictionException, DecisionException, 
	FailureException {
		//sanity check
		if (!canStep()) {
			throw new EngineStuckException();
		}

		//updates the information about the state before the step
		this.preStepSourceRow = this.currentState.getSourceRow();
		this.preStepStackSize = this.currentState.getStackSize();
		
		//steps
		final Algorithm algo = this.dispatcher.select(this.currentState.getInstruction());
		try {
			algo.exec(this.currentState, this.ctx);
		} catch (InterruptException e) {
		    //nothing to do
		} catch (ClasspathException | CannotManageStateException | ThreadStackEmptyException | 
			    ContradictionException | DecisionException | FailureException | 
			    UnexpectedInternalException e) {
			this.stopCurrentTrace();
			throw e;
		} 
		
		this.someReferenceNotExpanded = algo.someReferenceNotExpanded();
		this.nonExpandedReferencesOrigins = algo.nonExpandedReferencesOrigins();
		this.nonExpandedReferencesTypes = algo.nonExpandedReferencesTypes();

		//updates the current state and calculates return value
		final BranchPoint retVal;
		if (this.ctx.stateTree.createdBranch()) {
			retVal = this.ctx.stateTree.nextBranch();
			this.currentState = this.ctx.stateTree.nextState();
		} else {
			retVal = null;
			this.currentState.incSequenceNumber();
		}
    	
		//updates the counters for depth/count scope
        if (this.currentState.branchingDecision()) {
        	this.currentState.incDepth();
        	this.currentState.resetCount();
        } else {
        	this.currentState.incCount();
        }

		//synchronizes the decision procedure with the path condition
		this.ctx.decisionProcedure.addAssumptions(this.currentState.getLastPathConditionPushedClauses());
		this.currentState.resetLastPathConditionClauses();

		//manages variable observation
		this.vom.notifyObservers(retVal);

		//updates stats
		if (this.analyzedStates < Long.MAX_VALUE) { 
			++this.analyzedStates;
		}

		//returns
		return retVal;
	}
	
	/**
	 * Returns the engine's current JVM state.
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
	 *         {@link #step} method has been invoked.  
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
	 * Adds a branch point to the symbolic execution.
	 * 
	 * @return the created {@link BranchPoint}.
	 */
	public BranchPoint addBranchPoint() {
		final State s = (State) this.currentState.clone();
		this.ctx.stateTree.addBranchPoint(s, "MANUAL");
		final BranchPoint retVal = this.ctx.stateTree.nextBranch();
		this.vom.saveObservedVariablesValues(retVal);
		return retVal;
	}
	
	/**
	 * Stops the execution along the current trace.
	 */
	public void stopCurrentTrace() {
		this.currentState.setStuckStop();
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
	 * Checks whether a subsequent call to {@link #backtrack()} 
	 * will yield the last state in a branch.
	 * 
	 * @return {@code true} iff it will yield the last state in a branch.
	 * @throws CannotBacktrackException iff <code>this.</code>{@link #canBacktrack}()<code> == false</code> 
	 *         before the method is invoked.
	 */
	public boolean willBacktrackToLastInBranch() throws CannotBacktrackException {
		if (!this.canBacktrack()) {
			throw new CannotBacktrackException();
		}
		return this.ctx.stateTree.nextIsLastInCurrentBranch();
	}

	/**
	 * Backtracks the execution to the next pending branch.
	 * 
	 * @return the {@link BranchPoint} of the next pending branch.
	 * @throws CannotBacktrackException iff {@code this.}{@link #canBacktrack}{@code () == false} 
	 *         before the method is invoked.
	 * @throws DecisionBacktrackException iff the decision procedure fails for 
	 *         any reason. 
	 */
	public BranchPoint backtrack() 
	throws CannotBacktrackException, DecisionBacktrackException {
		//TODO dubious correctness of this implementation
		if (!this.canBacktrack()) {
			throw new CannotBacktrackException();
		}

		final boolean isLast = this.ctx.stateTree.nextIsLastInCurrentBranch();
		final BranchPoint bp = this.ctx.stateTree.nextBranch();
		
        try {
			this.currentState = this.ctx.stateTree.nextState();
			final Collection<Clause> currentAssumptions = this.currentState.getPathCondition();
			this.ctx.decisionProcedure.setAssumptions(currentAssumptions);
			this.currentState.resetLastPathConditionClauses();
		} catch (DecisionException e) {
			throw new DecisionBacktrackException(e);
		}
		
		//updates the counters for depth/count scope
        if (this.currentState.branchingDecision()) {
        	this.currentState.incDepth();
        	this.currentState.resetCount();
        } else {
        	this.currentState.incCount();
        }

		this.vom.restoreObservedVariablesValues(bp, isLast);
		
		return bp;
	}
	
	
	/**
	 * Test whether some of the references resolved by the last
	 * decision procedure call has not been expanded.
	 * 
	 * @return a {@code boolean}.
	 */
	public boolean someReferenceNotExpanded() {
		return this.someReferenceNotExpanded;
	}
	
	/**
	 * Returns a list of the origins of the references 
	 * resolved but not expanded by the last decision 
	 * procedure call.

	 * @return a {@link String}, a comma-separated
	 *         list of origins. 
	 */
	public String getNonExpandedReferencesOrigins() {
		return this.nonExpandedReferencesOrigins;
	}
	
	
	/**
	 * Returns a list of the static types of the references 
	 * resolved but not expanded by the last decision 
	 * procedure call.

	 * @return a {@link String}, a comma-separated
	 *         list of class names. 
	 */
	public String getNonExpandedReferencesTypes() {
		return this.nonExpandedReferencesTypes;
	}
	
	/**
	 * Checks whether the current bytecode is in a different source row
	 * than the previous one.
	 * 
	 * @return {@code true} iff the current bytecode is in a source code 
	 *         statement different from the previous one, or the current 
	 *         state is stuck.
	 * @throws ThreadStackEmptyException 
	 */
	public boolean sourceRowChanged() throws ThreadStackEmptyException {
		return (this.currentState.isStuck() || 
				(this.currentState.getSourceRow() != this.preStepSourceRow));
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
				(this.currentState.getStackSize() != this.preStepStackSize));
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
		final byte currentInstruction = this.currentState.getInstruction(); 
		return (
				currentInstruction == Opcodes.OP_INVOKEVIRTUAL||
				currentInstruction == Opcodes.OP_INVOKESPECIAL ||
				currentInstruction == Opcodes.OP_INVOKESTATIC ||
				currentInstruction == Opcodes.OP_INVOKEINTERFACE ||
				currentInstruction == Opcodes.OP_IRETURN ||
				currentInstruction == Opcodes.OP_FRETURN ||
				currentInstruction == Opcodes.OP_DRETURN ||
				currentInstruction == Opcodes.OP_ARETURN ||
				currentInstruction == Opcodes.OP_RETURN ||
				currentInstruction == Opcodes.OP_ATHROW
		);
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