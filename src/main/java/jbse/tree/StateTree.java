package jbse.tree;

import java.util.LinkedList;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;

/**
 * Class storing the {@link State}s in the symbolic execution
 * tree which have been discovered but not yet analyzed.
 * 
 * @author Pietro Braione
 * @author unknown
 */

public class StateTree {
	public static final String IDENTIFIER_SEPARATOR_COMPACT = ".";
	public static final String IDENTIFIER_DEFAULT_COMPACT = IDENTIFIER_SEPARATOR_COMPACT + "1";
	public static final String IDENTIFIER_SEPARATOR_LONG = "|";
	public static final String IDENTIFIER_DEFAULT_LONG = "ROOT";

	/**
	 * Enumeration of the different kinds of state identifiers.
	 * 
	 * @author Pietro Braione
	 */
	public static enum StateIdentificationMode { 
		/** 
		 * Each branch is identified by a number
		 * which represents the extraction order 
		 * from this tree. This identification is highly dependent
		 * on the decision procedure, which may prune some branches,
		 * but it is compact and exec-faithful (i.e., the  
		 * lexicographic order of branch identifiers reflects the
		 * visiting order of the symbolic execution).
		 */
		COMPACT, 
		
		REPLICABLE,
		
		/**
		 * Each branch is identified by a complex string 
		 * identifier reflecting the decision which generated it.
		 * This identification may be complex and not exec-faithful, 
		 * but univocally identifies symbolic execution
		 * traces up to target code recompilation.
		 */
		LONG;
	};
	
	/**
	 * Enumeration indicating how many branches will be created.
	 * 
	 * @author Pietro Braione
	 */
	public static enum BreadthMode {
		/**
		 * Create a branch only when a decision produces
		 * at least two different results. If the execution
		 * is guided, it will not produce any branch. 
		 * This yields the most breadth-compact tree. 
		 */
		MORE_THAN_ONE,
		
		/**
		 * Creates a branch only when a decision involving
		 * symbolic values is taken, filtering out all the
		 * symbolic decisions that have been resolved before
		 * (just on references).
		 */
		ALL_DECISIONS_NONTRIVIAL,
		
		/**
		 * Create a branch whenever a decision involving 
		 * symbolic values is taken, independently on 
		 * the number of possible outcomes.
		 */
		ALL_DECISIONS_SYMBOLIC,
		
		/**
		 * Create a branch whenever we hit a bytecode that
		 * may invoke a decision procedure, independently
		 * on whether all the involved values are concrete
		 * or not.
		 */
		ALL_DECISIONS;
	}

	/**
	 * A Memento for tree branches.
	 * 
	 * @author Pietro Braione
	 */
	public static class BranchPoint { }
	
	/** 
	 * Private class gathering information on a branch.
	 * 
	 * @author Pietro Braione
	 */ 
	private static class BranchInfo {
        /** A {@link BranchPoint}. */
		BranchPoint branch;

        /** 
         * The total number of states in the branch identified by {@code branch}. 
         * Used for COMPACT branch identification.
         */ 
        int totalStates;

        /** 
         * The number of states of {@code branch} already emitted. 
         * Used for COMPACT branch identification.
         */
        int emittedStates;
        
        /** 
         * Constructor for branch identification.
         */
        BranchInfo() {
            this.branch = new BranchPoint();
            this.totalStates = 0;
            this.emittedStates = 0;
        }
    }
	
	/** State identification mode. */
	private final StateIdentificationMode stateIdMode;
	
	/** Breadth mode. */
	private final BreadthMode breadthMode;

	/** Buffer of the inserted {@link State}s. */
	private final LinkedList<State> stateBuffer = new LinkedList<State>();

	/** Buffer of the inserted {@link BranchInfo}s. */
	private final LinkedList<BranchInfo> branchList = new LinkedList<BranchInfo>();

	/** 
	 * Flag indicating whether the tree level has been increased 
	 * since the last inspection by invocation of {@link #createdBranch()}.
	 */
	private boolean createdBranch = false;
	
	/** 
	 * Flag indicating whether the next state to be stored in the StateTree 
	 * is the first one, so it is preserved.
	 */
	private boolean nextIsInitialState = true;
	
	/**
	 * Constructor.
	 */
    public StateTree(StateIdentificationMode stateIdMode, BreadthMode breadthMode) {
    	this.stateIdMode = stateIdMode;
    	this.breadthMode = breadthMode;
		this.branchList.addFirst(new BranchInfo());
    }
    
    /**
     * Returns the branch identification mode.
     * 
     * @return a {@link StateIdentificationMode}.
     */
    public StateIdentificationMode getBranchIdentificationMode() {
    	return this.stateIdMode;
    }
	
    /**
     * Adds the initial state to the store. 
     * 
     * @param s the {@link State} to be added.
     */
    public void addInitialState(State s) {
    	this.add(s);
    	if (this.nextIsInitialState) {
    		s.appendToIdentifier((this.stateIdMode == StateIdentificationMode.COMPACT) ? 
    						IDENTIFIER_DEFAULT_COMPACT : IDENTIFIER_DEFAULT_LONG);
            s.resetDepth();
            s.resetCount();
	    } else {
	    	throw new UnexpectedInternalException(); //TODO define a better exception
    	}
    }

    /**
     * Adds a noninitial state to the store.
     * 
     * @param s the {@link State} to be added.
     * @param branchNumber a {@link int}, the number of the branch starting from {@code s}
     *        (used when the state identification mode is {@link StateIdentificationMode#REPLICABLE}).
     * @param branchIdentifier a {@link String}, the identifier of the branch starting from {@code s}
     *        (used when the state identification mode is {@link StateIdentificationMode#LONG}).
     */
    public void addState(State s, int branchNumber, String branchIdentifier) {
    	if (this.nextIsInitialState) {
        	throw new UnexpectedInternalException(); //TODO define a better exception
	    } 
	    	
    	//updates the state identifier
	    if (this.stateIdMode == StateIdentificationMode.REPLICABLE) {
	   		s.appendToIdentifier(IDENTIFIER_SEPARATOR_LONG + branchNumber);
	    } else if (this.stateIdMode == StateIdentificationMode.LONG) {
	   		s.appendToIdentifier(IDENTIFIER_SEPARATOR_LONG + branchIdentifier);
    	} //else (compact id) do nothing, nextState() will update it
        
    	add(s);
    }
    
    /**
     * Checks whether some {@link State} can be emitted.
     * 
     * @return true iff the store has one or more states 
     *              to emit.
     */
    public boolean hasStates() {
        return !this.stateBuffer.isEmpty();
    }
    
    /**
     * Checks whether the next state in this {@link StateTree} is the last in a branch.
     *  
     * @return {@code true} iff the next state in the store, that would be returned by a 
     *         call to {@link #nextState()}, is the last state of its branch.
     * @throws NoSuchElementException if {@link #hasStates()} {@code == false}.
     */
    public boolean nextIsLastInCurrentBranch() {
        final BranchInfo b = this.branchList.getFirst();
        return (b.emittedStates == b.totalStates - 1);
    }
    
    /**
     * Removes the next state from the store and emits it.
     * 
     * @return the {@link State} removed from the store.
     * @throws NoSuchElementException if {@link #hasStates()} {@code == false}.
     */
    public State nextState() {
        final State s = this.stateBuffer.getFirst();
        final BranchInfo b = this.branchList.getFirst();
        ++b.emittedStates;
        if (this.stateIdMode == StateIdentificationMode.COMPACT && !this.nextIsInitialState) {
        	s.appendToIdentifier(IDENTIFIER_SEPARATOR_COMPACT + String.valueOf(b.emittedStates));
        } //else, the identifier has been already set by addState
        this.nextIsInitialState = false;
        s.resetSequenceNumber();
        if (b.emittedStates == b.totalStates) {
            this.branchList.removeFirst();
        }
        return stateBuffer.removeFirst();
    }
    
    /**
     * Possibly increases by one the level of the tree. 
     * Note that increasing the level without adding a 
     * {@code State} will crash the engine.
     * 
     * @param moreThanOneResult {@code true} iff the 
     *        created branch will have than one state. 
     * @param trivial iff the branch originates from a 
     *        trivial decision.
     * @param concrete iff the branch originates from a 
     *        concrete decision.
     * @param noDecision iff the branch originates from a
     *        bytecode that takes no decision.
     * @return {@code true} iff the method has increased
     *         the tree level.
     */
    public boolean possiblyAddBranchPoint(boolean moreThanOneResult, boolean trivial, boolean concrete, boolean noDecision) {
		boolean retVal = moreThanOneResult;
    	switch (this.breadthMode) {
    	case MORE_THAN_ONE:
    		break;
    	case ALL_DECISIONS_NONTRIVIAL:
    		retVal = retVal || !trivial;
    		break;
    	case ALL_DECISIONS_SYMBOLIC:
			retVal = retVal || !concrete;
			break;
    	case ALL_DECISIONS:
    		retVal = retVal || !noDecision;
    		break;
    	default: 
    		throw new UnexpectedInternalException("Unexpected breadth mode " + this.breadthMode + ".");	    		
    	}
		
		if (retVal) {
			addBranchPoint();
		}
		
		return retVal;
    }
    
    /**
     * Increases by one the level of the tree and 
     * adds a {@code State}.
     * 
     * @param state the {@link State} to be added
     * @param id the identifier for {@code state}. 
     */
    public void addBranchPoint(State state, String id) {
    	addBranchPoint();
		addState(state, 1, id); //exactly one state in the branch
    }
    
    /**
     * Increases by one the level of the tree. Note that 
     * increasing the level without adding a {@code State}
     * will crash the engine.
     */
    private void addBranchPoint() {
		this.branchList.addFirst(new BranchInfo());
		this.createdBranch = true;
    }
    
    /**
     * Checks whether a new level of the tree has been created 
     * since the last invocation of this method.
     * 
     * @return {@code true} if {@link #addBranch}
     *         has been invoked since the previous invocation 
     *         of {@code createdBranch}, {@code false} 
     *         otherwise.
     */
    public boolean createdBranch() {
        final boolean retval = this.createdBranch;
        
        this.createdBranch = false;
        return retval;
    }
    
    /**
     * Returns the next branch point.
     * 
     * @return the {@link BranchPoint} associated to the next initial 
     *         state stored in the initializer, as it would be returned 
     *         by a call to {@link nextState}, 
     *         or {@code null} in the case such state exists.  
     */
    public BranchPoint nextBranch() {
        if (this.branchList.isEmpty()) {
            return null;
        } else {
            return this.branchList.getFirst().branch;
        }
    }    

    /**
     * Adds a state to the buffer and increases the 
     * total count of states in the branch.
     * 
     * @param s the {@link State} to be added.
     */
    private void add(State s) {
    	this.stateBuffer.addFirst(s);
        ++(this.branchList.getFirst().totalStates);
    }
}
