package jbse.tree;

import java.util.LinkedList;

import jbse.common.exc.InvalidInputException;
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
    public static final String IDENTIFIER_SEPARATOR_NONCOMPACT = "|";
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
     * Enumeration of the different kinds of states that are
     * extracted from this state tree.
     * 
     * @author Pietro Braione
     */
    private static enum StateKind {
        /** 
         * Before the initial state, there are all the states
         * that bootstrap the JVM.
         */
        PRE_INITIAL, 
        
        /**
         * The state where the next bytecode to be executed
         * is the first bytecode of the root method.
         */
        INITIAL, 
        
        /** All the states after the initial one. */
        POST_INITIAL 
    }

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

    
    /** Which kind of state is the next that can be extracted? */
    private StateKind nextStateIs = StateKind.INITIAL;

    /**
     * Constructor.
     */
    public StateTree(StateIdentificationMode stateIdMode, BreadthMode breadthMode) {
        this.stateIdMode = stateIdMode;
        this.breadthMode = breadthMode;
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
     * The next state to be inserted/extracted 
     * will be pre-initial.
     */
    public void nextIsPreInitial() {
       this.nextStateIs = StateKind.PRE_INITIAL; 
    }

    /**
     * Adds a state to the store without specifying
     * its branch identification. This method works
     * when the state identification mode is compact.
     * 
     * @param s the {@link State} to be added.
     * @throws InvalidInputException if this method is
     *         used to add a state when the state identification
     *         mode is replicable or long, or if it is invoked
     *         when there is a pre-initial or initial state to be emitted.
     */
    public void addState(State s) throws InvalidInputException {
        if (this.stateIdMode == StateIdentificationMode.COMPACT) {
            if (this.nextStateIs == StateKind.PRE_INITIAL) {
                if (hasStates()) {
                    throw new InvalidInputException("Tried to add a state with a not yet emitted pre-initial state.");
                }
                s.clearIdentifier();
                s.resetDepth();
                s.resetCount();
                addBranchPoint();
            } else if (this.nextStateIs == StateKind.INITIAL) {
                if (hasStates()) {
                    throw new InvalidInputException("Tried to add a state with a not yet emitted initial state.");
                }
                s.appendToIdentifier(IDENTIFIER_DEFAULT_COMPACT);
                s.resetDepth();
                s.resetCount();
                s.resetSequenceNumber();
                addBranchPoint();
            } else { //(this.nextStateIs == StateKind.POST_INITIAL)
                //does not update state identifier, in compact mode nextState() will update it            
                s.resetSequenceNumber();
            }
            add(s);
        } else {
            throw new InvalidInputException("Tried to add a state without specifying its branch identification in replicable or long identification mode.");
        }
    }

    /**
     * Adds a state to the store. This method works
     * for the post-initial states.
     * 
     * @param s the {@link State} to be added.
     * @param branchNumber a {@link int}, the number of the branch starting from {@code s}
     *        (used when the state identification mode is {@link StateIdentificationMode#REPLICABLE}).
     * @param branchIdentifier a {@link String}, the identifier of the branch starting from {@code s}
     *        (used when the state identification mode is {@link StateIdentificationMode#LONG}).
     * @throws InvalidInputException if this method is used to add a pre-initial 
     *         or initial state.
     */
    public void addState(State s, int branchNumber, String branchIdentifier) throws InvalidInputException {
        if (this.stateIdMode == StateIdentificationMode.REPLICABLE || this.stateIdMode == StateIdentificationMode.LONG) {
            if (this.nextStateIs == StateKind.POST_INITIAL) {
                //updates the state identifier
                if (this.stateIdMode == StateIdentificationMode.REPLICABLE) {
                    s.appendToIdentifier(IDENTIFIER_SEPARATOR_NONCOMPACT + branchNumber);
                } else { // (this.stateIdMode == StateIdentificationMode.LONG)
                    s.appendToIdentifier(IDENTIFIER_SEPARATOR_NONCOMPACT + branchIdentifier);
                }
                s.resetSequenceNumber();
                add(s);
            } else { 
                throw new InvalidInputException("Tried to add a pre-initial or initial state by specifing its branch identification.");
            }
        } else {
            //falls back on compact mode method
            addState(s);
        }
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
        final State s = this.stateBuffer.removeFirst();
        final BranchInfo b = this.branchList.getFirst();
        ++b.emittedStates;
        if (b.emittedStates == b.totalStates) {
            this.branchList.removeFirst();
        }
        
        if (this.nextStateIs == StateKind.POST_INITIAL && this.stateIdMode == StateIdentificationMode.COMPACT) {
            s.appendToIdentifier(IDENTIFIER_SEPARATOR_COMPACT + String.valueOf(b.emittedStates));
        } //else, the identifier has been already set by addState/addInitialState
        
        if (this.nextStateIs == StateKind.PRE_INITIAL) {
            this.nextStateIs = StateKind.INITIAL;
        } else if (this.nextStateIs == StateKind.INITIAL) {
            this.nextStateIs = StateKind.POST_INITIAL;
        } //else, this.nextStateIs does not change

        return s;
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
     * @throws InvalidInputException if this method is 
     *         used to add a pre-initial or initial state, 
     *         or if it is invoked when there is a 
     *         pre-initial or initial state to be emitted.
     */
    public void addBranchPoint(State state, String id) throws InvalidInputException {
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
     * @return {@code true} if {@link #addBranchPoint}
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
