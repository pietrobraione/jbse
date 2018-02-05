package jbse.jvm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.StateTree;
import jbse.val.Calculator;

/**
 * Encapsulates an {@link Engine}'s parameters. The 
 * most important ones are:
 * 
 * <ul>
 * <li>A {@link DecisionProcedureAlgorithms} for pruning
 * unfeasible branches;</li>
 * <li>The initial state of the symbolic execution, or
 * alternatively:
 * <ul>
 * <li>A classpath (. by default);</li>
 * <li>The signature of the method to be symbolically executed;</li> 
 * <li>A {@link Calculator} for simplifying symbolic expressions;</li>
 * </ul>
 * </li>
 * <li>The signatures of the methods that must be treated as uninterpreted
 * functions, or for which there is a meta-level overriding implementation;</li>
 * <li>The signatures of the trigger instrumentation methods plus the 
 * reference resolution events that fire them;</li> 
 * <li>A {@link StateIdentificationMode} and a {@link BreadthMode};</li>
 * <li>A set of {@link ExecutionObserver}s plus the
 * specification of the variables they observe (none by default).</li> 
 * </ul> 
 * 
 * @author Pietro Braione
 *
 */
public final class EngineParameters implements Cloneable {
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
		COMPACT(StateTree.StateIdentificationMode.COMPACT), 
		
        /** 
         * Each branch is identified by a number
         * statically assigned according to the type of the branch. 
         * This identification is still compact but not exec-faithful, 
         * and is fragile on the number of siblings, that might
         * depend on the decision procedure and on the preconditions.
         */
		REPLICABLE(StateTree.StateIdentificationMode.REPLICABLE),

		/**
		 * Each branch is identified by a complex string 
		 * identifier reflecting the decision which generated it.
		 * This identification may be complex and not exec-faithful, 
		 * but gives an unique identifier to symbolic execution
		 * traces up to target code recompilation.
		 */
		LONG(StateTree.StateIdentificationMode.LONG);

		private final StateTree.StateIdentificationMode internal;
		
		private StateIdentificationMode(StateTree.StateIdentificationMode internal) {
			this.internal = internal;
		}
		
		public final StateTree.StateIdentificationMode toInternal() {
			return this.internal;
		}
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
		MORE_THAN_ONE(StateTree.BreadthMode.MORE_THAN_ONE),
		
		/**
		 * Creates a branch only when a decision involving
		 * symbolic values is taken, filtering out all the
		 * symbolic decisions that have been resolved before
		 * (just on references).
		 */
		ALL_DECISIONS_NONTRIVIAL(StateTree.BreadthMode.ALL_DECISIONS_NONTRIVIAL),
		
		/**
		 * Create a branch whenever a decision involving 
		 * symbolic values is taken, independently on 
		 * the number of possible outcomes.
		 */
		ALL_DECISIONS_SYMBOLIC(StateTree.BreadthMode.ALL_DECISIONS_SYMBOLIC),
		
		/**
		 * Create a branch whenever we hit a bytecode that
		 * may invoke a decision procedure, independently
		 * on whether all the involved values are concrete
		 * or not.
		 */
		ALL_DECISIONS(StateTree.BreadthMode.ALL_DECISIONS);
	    
	    private final StateTree.BreadthMode internal;
	    
	    private BreadthMode(StateTree.BreadthMode internal) {
	        this.internal = internal;
	    }

		public final StateTree.BreadthMode toInternal() {
		    return this.internal;
		}
	}
	
	/** The state identification mode. */
	private StateIdentificationMode stateIdMode = StateIdentificationMode.COMPACT;
	
	/** The breadth mode. */
	private BreadthMode breadthMode = BreadthMode.MORE_THAN_ONE;

	/** 
	 * The initial {@link State} of the symbolic execution, or
	 * {@code null} iff an initial state for a method invocation 
	 * must be created by the runner; by default it is {@code null}.
	 */
	private State initialState = null;
	
	/**  
	 * The classpath; overridden by {@code initialState}'s classpath
	 * when {@code initialState != null}.
	 */
	private ArrayList<String> paths = new ArrayList<>();

	/** 
	 * The {@link Calculator}; overridden by {@code initialState}'s 
	 * calculator when {@code initialState != null}. 
	 */
	private Calculator calc = null;
	
	/** The decision procedure. */
	private DecisionProcedureAlgorithms decisionProcedure = null;
	
	/** The signatures of the variables observed by {@code this.observers}. */
	private ArrayList<Signature> observedVars = new ArrayList<>();

	/** The {@code ExecutionObserver}s. */
	private ArrayList<ExecutionObserver> observers = new ArrayList<>();
	
	/** The {@link TriggerRulesRepo}, containing all the trigger rules. */
	private TriggerRulesRepo repoTrigger = new TriggerRulesRepo();
	
	/** The expansion backdoor. */
	private HashMap<String, Set<String>> expansionBackdoor = new HashMap<>();

	/** The methods overridden at the meta-level. */
	private ArrayList<String[]> metaOverridden = new ArrayList<>();

    /** The methods to be handled as uninterpreted functions. */
	private ArrayList<String[]> uninterpreted = new ArrayList<>();

	/**  
	 * The signature of the method to be executed; overridden by {@code initialState}'s 
	 * current method when {@code initialState != null}.
	 */
	private Signature methodSignature = null;

	/**
	 * Constructor.
	 */
	public EngineParameters() { }

	/**
	 * Sets the decision procedure to be used during symbolic execution.
	 * 
	 * @param decisionProcedure a {@link DecisionProcedureAlgorithms}.
	 */
	public void setDecisionProcedure(DecisionProcedureAlgorithms decisionProcedure) {
		this.decisionProcedure = decisionProcedure;
	}
	
	/**
	 * Gets the decision procedure.
	 * 
	 * @return a {@link DecisionProcedureAlgorithms}.
	 */
	public DecisionProcedureAlgorithms getDecisionProcedure() {
		return this.decisionProcedure;
	}

	/**
	 * Sets the state identification mode, i.e., how a state will be
	 * identified.
	 * 
	 * @param stateIdMode a {@link StateIdentificationMode}.
	 * @throws NullPointerException if {@code stateIdMode == null}.
	 */
	public void setStateIdentificationMode(StateIdentificationMode stateIdMode) {
		if (stateIdMode == null) {
			throw new NullPointerException();
		}
		this.stateIdMode = stateIdMode;
	}
	
	/**
	 * Gets the state identification mode.
	 * 
	 * @return the {@link StateIdentificationMode} set by the
	 *         last call to {@link #setStateIdentificationMode(StateIdentificationMode)}.
	 */
	public StateIdentificationMode getStateIdentificationMode() {
		return this.stateIdMode;
	}
	
	/**
	 * Sets the breadth mode, i.e., how many branches 
	 * will be created during execution.
	 * 
	 * @param breadthMode a {@link BreadthMode}.
	 * @throws NullPointerException if {@code breadthMode == null}.
	 */
	public void setBreadthMode(BreadthMode breadthMode) {
		if (breadthMode == null) {
			throw new NullPointerException();
		}
		this.breadthMode = breadthMode;
	}
	
	/**
	 * Gets the breadth mode.
	 * 
	 * @return the {@link BreadthMode} set by the
	 *         last call to {@link #setBreadthMode(BreadthMode)}.
	 */
	public BreadthMode getBreadthMode() {
		return this.breadthMode;
	}

	/** 
	 * Adds an {@link ExecutionObserver} performing additional
	 * actions when a field changes its value.
	 * 
	 * @param fldClassName the name of the class where the field
	 *        resides.
	 * @param fldType the type of the field.
	 * @param fldName the name of the field.
	 * @param observer an {@link ExecutionObserver}. It will be 
	 *        notified whenever the field {@code observedVar} of 
	 *        any instance of {@code className} is modified.
	 */ 
	public void addExecutionObserver(String fldClassName, String fldType, String fldName, ExecutionObserver observer) {
		final Signature sig = new Signature(fldClassName, fldType, fldName);
		this.observedVars.add(sig);
		this.observers.add(observer);
	}
	
	/**
	 * Clears the {@link ExecutionObserver}s.
	 */
	public void clearExecutionObservers() {
	    this.observedVars.clear();
	    this.observers.clear();
	}
	
	/**
	 * Returns the {@link Signature}s of the fields
	 * observed by some {@link ExecutionObserver}.
	 * 
	 * @return a {@link List}{@code <}{@link Signature}{@code >},
     *         in the order matching that of the return value 
     *         of {@link #getObservers()}.
	 */
	public List<Signature> getObservedFields() {
	    return new ArrayList<>(this.observedVars);
	}
	
	/**
     * Returns the {@link Signature}s of the fields
     * observed by some {@link ExecutionObserver}.
     * 
     * @return a {@link List}{@code <}{@link Signature}{@code >},
     *         in the order matching that of the return value 
     *         of {@link #getObservedFields()}.
	 */
	public List<ExecutionObserver> getObservers() {
	    return new ArrayList<>(this.observers);
	}

	/**
	 * Sets the initial state of the symbolic execution, and cancels the 
	 * effect of any previous call to {@link #addClasspath(String...)},
	 * {@link #setMethodSignature(String)}.
	 *  
	 * @param s a {@link State}.
	 */
	public void setInitialState(State s) { 
		this.initialState = s; 
		this.paths.clear();
		this.methodSignature = null;
		this.calc = null;
	}
	
	/**
	 * Gets the initial state of the symbolic execution (a safety copy).
	 * 
	 * @return the {@link State} set by the last call to 
	 *         {@link #setInitialState(State)} (possibly {@code null}).
	 */
	public State getInitialState() {
		if (this.initialState == null) {
			return null;
		} else {
			return this.initialState.clone();
		}
	}
	
	public void setCalculator(Calculator calc) {
		this.calc = calc;
		this.initialState = null;
	}

	public Calculator getCalculator() {
		if (this.initialState == null) {
			return this.calc;
		} else {
			return this.initialState.getCalculator();
		}
	}

	/**
	 * Sets the symbolic execution's classpath, and cancels the effect of any 
	 * previous call to {@link #setInitialState(State)}; the 
	 * default classpath is {@code "."}.
	 * 
	 * @param paths a varargs of {@link String}, 
	 *        the paths to be added to the classpath.
	 */
	public void addClasspath(String... paths) { 
		this.initialState = null; 
		Collections.addAll(this.paths, paths); 
	}
	
	/**
	 * Clears the symbolic execution's classpath.
	 */
	public void clearClasspath() {
	    this.paths.clear();
	}

	/**
	 * Returns the symbolic execution's classpath (a safety copy).
	 * 
	 * @return a {@link Classpath} object. 
	 */
	public Classpath getClasspath() {
		if (this.initialState == null) {
			return new Classpath(this.paths.toArray(ARRAY_OF_STRING)); //safety copy
		} else {
			return this.initialState.getClasspath();
		}
	}
	private static final String[] ARRAY_OF_STRING = { };
	    
    /**
     * Returns the {@link TriggerRulesRepo} 
     * containing all the trigger rules that
     * must be used.
     * 
     * @return a {@link TriggerRulesRepo}. It
     *         is the one that backs this
     *         {@link EngineParameters}, not a
     *         safety copy.
     */
    public TriggerRulesRepo getTriggerRulesRepo() {
        return this.repoTrigger;
    }
	
    /**
     * Returns the expansion backdoor.
     * 
     * @return a {@link Map}{@code <}{@link String}{@code , }{@link Set}{@code <}{@link String}{@code >>},
     *         associating class names with set of
     *         other class names. 
     *         It is the one that backs this
     *         {@link EngineParameters}, not a
     *         safety copy.
     */
	public Map<String, Set<String>> getExpansionBackdoor() {
	    return this.expansionBackdoor;
	}
	
    /**
     * Adds a trigger method that fires when some references are resolved by
     * expansion. Also adds a class to the expansion backdoor.
     * 
     * @param toExpand     the static type of the reference to be expanded. 
     *                     It must be {@code toExpand != null}.
     * @param originExp    a path expression describing the origin of the 
     *                     symbolic references which match this rule.
     *                     The path expression is a slash-separated list of field
     *                     names that starts from {ROOT}:x, indicating the 
     *                     parameter with name {@code x} of the root method 
     *                     invocation (including {@code this}).
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toExpand} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     expansions for this trigger to fire. 
     * @param triggerClassName 
     *                     the class of the trigger method. When {@code null}
     *                     no trigger method will fire (only the expansion backdoor
     *                     is updated).
     * @param triggerParametersSignature 
     *                     the types of the parameters of the trigger method. 
     *                     When {@code null} no trigger method will fire (only 
     *                     the expansion backdoor is updated).
     * @param triggerMethodName 
     *                     the name of the trigger method. When {@code null}
     *                     no trigger method will fire (only the expansion backdoor
     *                     is updated).
     * @param triggerParameter
     *                     the parameter to be passed to the trigger method. 
     */
	public void addExpandToTrigger(String toExpand, String originExp, String classAllowed, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
	    if (triggerClassName != null && triggerParametersSignature != null && triggerMethodName == null) {
	        this.repoTrigger.addExpandTo(toExpand, originExp, classAllowed, 
		            new Signature(triggerClassName, triggerParametersSignature, triggerMethodName), triggerParameter);
	    }
		
		//updates expansion backdoor
	    Set<String> classesAllowed = this.expansionBackdoor.get(toExpand);
	    if (classesAllowed == null) {
	        classesAllowed = new HashSet<>();
	        this.expansionBackdoor.put(toExpand, classesAllowed);
	    }
	    classesAllowed.add(classAllowed);	
	}
	
    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toResolve      the static type of the reference to be resolved. 
     *                       It must be {@code toResolve != null}.
     * @param originExp      a path expression describing the origin of the 
     *                       symbolic references which match this rule.
     *                       The path expression is a slash-separated list of field
     *                       names that starts from {ROOT}:x, indicating the 
     *                       parameter with name {@code x} of the root method 
     *                       invocation (including {@code this}).
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathAllowedExp a path expression describing the objects which are 
     *                       possible alias for this trigger to fire. 
     *                       The path expression is a slash-separated list of field
     *                       names that starts from {ROOT}:x, indicating the 
     *                       parameter with name {@code x} of the root method 
     *                       invocation (including {@code this}), or from 
     *                       {REF}, indicating a path starting from the origin 
     *                       of the reference matched by the left part of the rule. 
     *                       You can also use the special {UP} to move back in the 
     *                       path; for instance, if the reference matching 
     *                       {@code originExp} has origin 
     *                       {ROOT}:this/list/head/next/next, then you can use both 
     *                       {REF}/{UP}/{UP}/{UP} and {ROOT}:this/list to denote 
     *                       the field with name {@code list} of the object that is
     *                       referred by the {@code this} parameter of the root method
     *                       invocation.
     * @param triggerClassName 
     *                       the class of the trigger method.
     * @param triggerParametersSignature 
     *                       the types of the parameters of the trigger method.
     * @param triggerMethodName 
     *                       the name of the trigger method.
     * @param triggerParameter
     *                       the parameter to be passed to the trigger method. 
     */
	public void addResolveAliasOriginTrigger(String toResolve, String originExp, String pathAllowedExp, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
        this.repoTrigger.addResolveAliasOrigin(toResolve, originExp, pathAllowedExp, 
                new Signature(triggerClassName, triggerParametersSignature, triggerMethodName), triggerParameter);
	}

    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toResolve    the static type of the reference to be resolved. 
     *                     It must be {@code toResolve != null}.
     * @param originExp    a path expression describing the origin of the 
     *                     symbolic references which match this rule.
     *                     The path expression is a slash-separated list of field
     *                     names that starts from {ROOT}:x, indicating the 
     *                     parameter with name {@code x} of the root method 
     *                     invocation (including {@code this}).
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toResolve} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     expansions for this trigger to fire. 
     * @param triggerClassName 
     *                     the class of the trigger method.
     * @param triggerParametersSignature 
     *                     the types of the parameters of the trigger method.
     * @param triggerMethodName 
     *                     the name of the trigger method.
     * @param triggerParameter
     *                     the parameter to be passed to the trigger method. 
     */
	public void addResolveAliasInstanceofTrigger(String toResolve, String originExp, String classAllowed, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
	    this.repoTrigger.addResolveAliasInstanceof(toResolve, originExp, classAllowed,
	            new Signature(triggerClassName, triggerParametersSignature, triggerMethodName), triggerParameter);
	}

    /**
     * Adds a trigger method that fires when some references are resolved by
     * null.
     * 
     * @param toResolve the static type of the reference to be resolved. 
     *                  It must be {@code toResolve != null}.
     * @param originExp a path expression describing the origin of the 
     *                  symbolic references that match this rule.
     *                  The path expression is a slash-separated list of field
     *                  names that starts from {ROOT}:x, indicating the 
     *                  parameter with name {@code x} of the root method 
     *                  invocation (including {@code this}).
     *                  If {@code originExp == null}, all the symbolic 
     *                  references with static type {@code toResolve} 
     *                  will match. 
     * @param triggerClassName 
     *                  the class of the trigger method.
     * @param triggerParametersSignature 
     *                  the types of the parameters of the trigger method.
     * @param triggerMethodName 
     *                  the name of the trigger method.
     * @param triggerParameter
     *                  the parameter to be passed to the trigger method. 
     */ 
	public void addResolveNullTrigger(String toResolve, String originExp, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
	    this.repoTrigger.addResolveNull(toResolve, originExp,
	            new Signature(triggerClassName, triggerParametersSignature, triggerMethodName), triggerParameter);
	}

	/**
	 * Specifies an alternative, meta-level implementation of a method 
	 * that must override the standard one. 
	 * 
	 * @param className the name of the class containing the overridden method.
	 * @param descriptor the descriptor of the method.
	 * @param methodName the name of the method.
	 * @param metaDelegateClassName the name of a {@link Class} that implements
	 *        the semantics of calls to the {@code methodName} method.
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void addMetaOverridden(String className, String descriptor, String methodName, String metaDelegateClassName) {
		if (className == null || descriptor == null || methodName == null || metaDelegateClassName == null) {
			throw new NullPointerException();
		}
		this.metaOverridden.add(new String[] { className, descriptor, methodName, metaDelegateClassName });
	}
	
	/**
	 * Clears the specifications of the meta-level implementations 
     * of methods.
	 */
	public void clearMetaOverridden() {
	    this.metaOverridden.clear();
	}
	
	/**
	 * Returns the specifications of the meta-level implementations 
	 * of methods.
	 * 
	 * @return A {@link List}{@code <}{@link String}{@code []>}, 
     *         where each array is a 4-ple (method class name, 
     *         method parameters, method name, meta delegate
     *         class name).
	 */
	public ArrayList<String[]> getMetaOverridden() {
	    return new ArrayList<>(this.metaOverridden);
	}

	/**
	 * Specifies that a method must be treated as an uninterpreted pure
	 * function, rather than executed. 
	 * 
	 * @param className the name of the class containing the method not to be
	 *        interpreted.
         * @param descriptor the descriptor of the method. All the parameters types 
         *        in the descriptor must be primitive.
	 * @param methodName the name of the method.
	 * @param functionName a {@link String}, the name that will be given to 
	 *        the uninterpreted function.
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void addUninterpreted(String className, String descriptor, String methodName, String functionName) {
		if (className == null || descriptor == null || methodName == null || functionName == null) {
			throw new NullPointerException();
		}
		this.uninterpreted.add(new String[] { className, descriptor, methodName, functionName });
	}
	
	/**
	 * Clears the methods that must be treated as
     * uninterpreted pure functions.
	 */
	public void clearUninterpreted() {
	    this.uninterpreted.clear();
	}
	
	/**
	 * Returns the methods that must be treated as
	 * uninterpreted pure functions.
	 * 
	 * @return A {@link List}{@code <}{@link String}{@code []>}, 
	 *         where each array is a 4-ple (method class name, 
	 *         method parameters, method name, uninterpreted 
	 *         function name).
	 */
	public List<String[]> getUninterpreted() {
	    return new ArrayList<>(this.uninterpreted);
	}
	
	/**
	 * Sets the signature of the method which must be symbolically executed, 
	 * and cancels the effect of any previous call to {@link #setInitialState(State)}.
	 * 
	 * @param className the name of the class containing the method.
	 * @param descriptor the descriptor of the method.
	 * @param methodName the name of the method. 
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void setMethodSignature(String className, String descriptor, String methodName) { 
		if (className == null || descriptor == null || methodName == null) {
			throw new NullPointerException();
		}
		this.initialState = null; 
		this.methodSignature = new Signature(className, descriptor, methodName); 
	}
	
	/**
	 * Gets the signature of the method which must be symbolically executed.
	 * 
	 * @return a {@link Signature}, or {@code null} if no method signature
	 *         has been provided.
	 */
	public Signature getMethodSignature() {
		if (this.methodSignature == null && this.initialState == null) {
			return null;
		}
		if (this.methodSignature == null) {
			try {
				return this.initialState.getCurrentMethodSignature();
			} catch (ThreadStackEmptyException e) {
				return null;
			}
		}
		return this.methodSignature;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public EngineParameters clone() {
		final EngineParameters o;
		try {
			o = (EngineParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		if (this.initialState != null) {
			o.initialState = this.initialState.clone();
		}
		o.paths = (ArrayList<String>) this.paths.clone();
		//calc and decisionProcedure are *not* cloned
		o.observedVars = (ArrayList<Signature>) this.observedVars.clone();
		o.repoTrigger = this.repoTrigger.clone();
		o.expansionBackdoor = new HashMap<>();
		for (Map.Entry<String, Set<String>> e : o.expansionBackdoor.entrySet()) {
		    o.expansionBackdoor.put(e.getKey(), new HashSet<>(e.getValue()));
		}
		o.metaOverridden = (ArrayList<String[]>) this.metaOverridden.clone();
		o.uninterpreted = (ArrayList<String[]>) this.uninterpreted.clone();
		return o;
	}
}
