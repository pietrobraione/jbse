package jbse.jvm;

import java.util.ArrayList;
import java.util.Collections;

import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.State;
import jbse.rewr.CalculatorRewriting;
import jbse.tree.StateTree;

/**
 * Class encapsulating the protocol of an {@link Engine}'s 
 * parameters. These are:
 * 
 * <ul>
 * <li>The choice of a decision procedure for pruning
 * unfeasible branches; currently only Sicstus, CVC3 
 * and Z3 are supported.;
 * <li>The path of the solver's executable (. by default);</li>
 * <li>The initial state of the symbolic execution, or
 * alternatively:
 * <ul>
 * <li>A classpath and a source path (. by default);</li>
 * <li>The rules for class initialization and reference resolution 
 * (none by default);</li>
 * <li>The signature of the method to be symbolically executed;</li> 
 * <li>The signature of a variable which is assumed initially 
 * volatile (none by default);</li>
 * <li>A set of {@link ExecutionObserver}s together with the
 * specification of the variables they observe (none by default).</li> 
 * </ul>
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
		
		REPLICABLE(StateTree.StateIdentificationMode.REPRODUCIBLE),

		/**
		 * Each branch is identified by a complex string 
		 * identifier reflecting the decision which generated it.
		 * This identification may be complex and not exec-faithful, 
		 * but univocally identifies symbolic execution
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
		MORE_THAN_ONE {
			@Override
			public StateTree.BreadthMode toInternal() {
				return StateTree.BreadthMode.MORE_THAN_ONE;
			}
		},
		
		/**
		 * Creates a branch only when a decision involving
		 * symbolic values is taken, filtering out all the
		 * symbolic decisions that have been resolved before
		 * (just on references).
		 */
		ALL_DECISIONS_NONTRIVIAL {
			@Override
			public StateTree.BreadthMode toInternal() {
				return StateTree.BreadthMode.ALL_DECISIONS_NONTRIVIAL;
			}
		},
		
		/**
		 * Create a branch whenever a decision involving 
		 * symbolic values is taken, independently on 
		 * the number of possible outcomes.
		 */
		ALL_DECISIONS_SYMBOLIC {
			@Override
			public StateTree.BreadthMode toInternal() {
				return StateTree.BreadthMode.ALL_DECISIONS_SYMBOLIC;
			}
		},
		
		/**
		 * Create a branch whenever we hit a bytecode that
		 * may invoke a decision procedure, independently
		 * on whether all the involved values are concrete
		 * or not.
		 */
		ALL_DECISIONS {
			@Override
			public StateTree.BreadthMode toInternal() {
				return StateTree.BreadthMode.ALL_DECISIONS;
			}
		};

		public abstract StateTree.BreadthMode toInternal();
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
	 * The {@link CalculatorRewriting}; overridden by {@code initialState}'s 
	 * calculator when {@code initialState != null}. 
	 */
	private CalculatorRewriting calc = null;
	
	/** The decision procedure. */
	private DecisionProcedureAlgorithms decisionProcedure = null;
	
	/** Whether the symbolic execution should be guided along a concrete one. */
	private boolean guided = false;
	
	/** The signatures of the variables observed by {@code this.observers}. */
	ArrayList<Signature> observedVars = new ArrayList<>();

	/** The {@code ExecutionObserver}s. */
	ArrayList<ExecutionObserver> observers = new ArrayList<>();
	
	/** The expansion triggers, a list of {@link String} triples. */
	ArrayList<String[]> expandToTriggers = new ArrayList<>();

	/** 
	 * The alias resolution triggers (origin pattern), a list of 
	 * {@link String} triples. 
	 */
	ArrayList<String[]> resolveAliasOriginTriggers = new ArrayList<>();

	/** 
	 * The alias resolution triggers (instance of class), a list of 
	 * {@link String} triples. 
	 */
	ArrayList<String[]> resolveAliasInstanceofTriggers = new ArrayList<String[]>();
	
	/** The {@code null} resolution triggers, a list of string triples. */
	ArrayList<String[]> resolveNullTriggers = new ArrayList<>();

	ArrayList<String[]> metaOverridden = new ArrayList<String[]>();

	ArrayList<String[]> uninterpreted = new ArrayList<String[]>();

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
	 * Sets whether the symbolic execution is guided.
	 * 
	 * @param guided {@code true} iff the symbolic execution is guided.
	 */
	public void setGuided(boolean guided) {
		this.guided = guided;
	}
	
	/**
	 * Tests whether the symbolic execution is guided.
	 * 
	 * @return {@code true} iff the symbolic execution is guided.
	 */
	public boolean isGuided() {
		return this.guided;
	}

	/** 
	 * Adds an {@link ExecutionObserver} performing additional
	 * actions when a field changes its value.
	 * 
	 * @param className the name of the class where the field
	 *        resides.
	 * @param type the type of the field.
	 * @param observedVar the name of the field.
	 * @param observer an {@link ExecutionObserver}. It will be 
	 *        notified whenever the field {@code observedVar} of 
	 *        any instance of {@code className} is modified.
	 */ 
	public void addExecutionObserver(String className, String type, String observedVar, ExecutionObserver observer) {
		final Signature sig = new Signature(className, type, observedVar);
		this.observedVars.add(sig);
		this.observers.add(observer);
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
	
	public void setCalculator(CalculatorRewriting calc) {
		this.calc = calc;
		this.initialState = null;
	}

	public CalculatorRewriting getCalculator() {
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
	 * Returns the symbolic execution's classpath (a safety copy).
	 * 
	 * @return a {@link Classpath} object. 
	 */
	public Classpath getClasspath() {
		if (this.initialState == null) {
			return new Classpath(this.paths.toArray(FOO)); //safety copy
		} else {
			return this.initialState.getClasspath();
		}
	}
	private static final String[] FOO = { };
	
    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toExpand     the static type of the reference to be expanded. It must 
     *                     be {@code toExpand != null}.
     * @param originExp    an expression describing the origin of the 
     *                     symbolic references which match this replacement.
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toExpand} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     expansions for {@code toExpand}. During the 
     *                     symbolic execution, every symbolic reference with 
     *                     static type {@code toExpand} and origin matching 
     *                     {@code originExp}, will be expanded 
     *                     when necessary to a symbolic object with class 
     *                     {@code classAllowed}. If {@code originExp == null}, 
     *                     the matching {@link ReferenceSymbolic}s will not be expanded.
     * @param trigger      the instrumentation method to be triggered when this rule fires.
     */
	public void addExpandToTrigger(String toExpand, String originExp, String classAllowed, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
		this.expandToTriggers.add(new String[] { toExpand, originExp, classAllowed,
				triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter});
	}
	

    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathAllowedExp an expression describing the objects which are 
     *                       acceptable as alias for {@code toResolve}. During the 
     *                       symbolic execution, every symbolic reference with 
     *                       class {@code toResolve} and origin matching 
     *                       {@code originExp}, will be resolved 
     *                       when necessary to all the type- and epoch-compatible 
     *                       symbolic objects whose paths match
     *                       {@code pathAllowedExp} (use root to indicate
     *                       the root object, {REF} to indicate a path 
     *                       starting from the origin of the reference to expand, 
     *                       and {UP} to move back in the path; for instance, if 
     *                       the reference to expand has origin 
     *                       root/list/head/next/next, then {REF}/{UP}/{UP}/{UP} denotes 
     *                       the path root/list). If {@code pathAllowedExp == null}
     *                       the matching {@link ReferenceSymbolic} will not be
     *                       resolved by alias.
     * @param trigger        the instrumentation method to be triggered when this rule fires.
     */
	public void addResolveAliasOriginTrigger(String toResolve, String originExp, String pathAllowedExp, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
		this.resolveAliasOriginTriggers.add(new String[] { toResolve, originExp, pathAllowedExp, 
				triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter});
	}

    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param classAllowed   the name of the class whose instances are possible 
     *                       aliases for {@code toResolve}. During the 
     *                       symbolic execution, every symbolic reference with 
     *                       static type {@code toResolve} and origin matching 
     *                       {@code originExp}, will be resolved 
     *                       when necessary to all the epoch-compatible symbolic objects 
     *                       with class
     *                       {@code classAllowed}. If {@code classAllowed == null}
     *                       the matching {@link ReferenceSymbolic} will not be
     *                       resolved by alias.
     * @param trigger        the instrumentation method to be triggered when this rule fires.
     */
	public void addResolveAliasInstanceofTrigger(String toResolve, String originExp, String classAllowed, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
		this.resolveAliasInstanceofTriggers.add(new String[] { toResolve, originExp, classAllowed, 
				triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter});
	}

    /**
     * Adds a trigger method that fires when some references are resolved to
     * null.
     * 
     * @param toResolve      the static type of the reference to be resolved. It must 
     *                       be {@code toResolve != null}.
     * @param originExp      an expression describing the origin of the 
     *                       symbolic references which match this replacement.
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match.
     * @param trigger        the instrumentation method to be triggered when this 
     *                       rule fires.
     */ 
	public void addResolveNullTrigger(String toResolve, String originExp, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
		this.resolveNullTriggers.add(new String[] { toResolve,  originExp, triggerClassName, 
				triggerParametersSignature, triggerMethodName, triggerParameter});
	}

	/**
	 * Specifies an alternative, meta-level implementation of a method 
	 * that must override the standard one. 
	 * 
	 * @param className the name of the class containing the overridden method.
	 * @param parametersSignature the types of the method parameters.
	 * @param methodName the name of the method.
	 * @param metaDelegateClassName the name of a {@link Class} that implements
	 *        the semantics of calls to the {@code methodName} method.
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void addMetaOverridden(String className, String parametersSignature, String methodName, String metaDelegateClassName) {
		if (className == null || parametersSignature == null || methodName == null || metaDelegateClassName == null) {
			throw new NullPointerException();
		}
		this.metaOverridden.add(new String[] { className, parametersSignature, methodName, metaDelegateClassName });
	}

	/**
	 * Specifies that a method must be treated as an uninterpreted pure
	 * function, rather than executed. 
	 * 
	 * @param className the name of the class containing the method not to be
	 *        interpreted.
	 * @param parametersSignature the types of the method parameters.
	 * @param methodName the name of the method.
	 * @param functionName a {@link String}, the name that will be given to 
	 *        the uninterpreted function.
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void addUninterpreted(String className, String parametersSignature, String methodName, String functionName) {
		if (className == null || parametersSignature == null || methodName == null || functionName == null) {
			throw new NullPointerException();
		}
		this.uninterpreted.add(new String[] { className, parametersSignature, methodName, functionName });
	}
	
	/**
	 * Sets the signature of the method which must be symbolically executed, 
	 * and cancels the effect of any previous call to {@link #setInitialState(State)}.
	 * 
	 * @param className the name of the class containing the method.
	 * @param parametersSignature the types of the method parameters.
	 * @param methodName the name of the method. 
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void setMethodSignature(String className, String parametersSignature, String methodName) { 
		if (className == null || parametersSignature == null || methodName == null) {
			throw new NullPointerException();
		}
		this.initialState = null; 
		this.methodSignature = new Signature(className, parametersSignature, methodName); 
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
		o.resolveNullTriggers = (ArrayList<String[]>) this.resolveNullTriggers.clone();
		o.expandToTriggers = (ArrayList<String[]>) this.expandToTriggers.clone();
		o.resolveAliasOriginTriggers = (ArrayList<String[]>) this.resolveAliasOriginTriggers.clone();
		o.resolveAliasInstanceofTriggers = (ArrayList<String[]>) this.resolveAliasInstanceofTriggers.clone();
		o.metaOverridden = (ArrayList<String[]>) this.metaOverridden.clone();
		o.uninterpreted = (ArrayList<String[]>) this.uninterpreted.clone();
		return o;
	}
}
