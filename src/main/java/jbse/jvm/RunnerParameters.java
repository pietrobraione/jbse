package jbse.jvm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.jvm.EngineParameters.BreadthMode;
import jbse.jvm.EngineParameters.StateIdentificationMode;
import jbse.jvm.Runner.Actions;
import jbse.mem.State;
import jbse.val.Calculator;

/**
 * Encapsulates a {@link Runner}'s parameters. The most
 * important ones are:
 * 
 * <ul>
 * <li>The {@link EngineParameters};</li>
 * <li>The {@link Actions} to be performed at prescribed points 
 * of the symbolic execution;</li>
 * <li>Bounds for limiting the (scope of the) exploration of the 
 * symbolic execution state space.</li>
 * </ul>
 * 
 * @author Pietro Braione
 *
 */
public final class RunnerParameters implements Cloneable {
	/** The engine parameters */
	private EngineParameters engineParameters;
	
	/** The heap scope. */
	private HashMap<String, Integer> heapScopeStatic = new HashMap<>();
	
	/** The (function-based) heap scope used for concretization. */
	private HashMap<String, Function<State, Integer>> heapScopeComputed = new HashMap<>();
	
	/** The timeout. */
	private long timeout = 0;

	/** The depth scope. */
	private int depthScope = 0;

	/** The count scope. */
	private int countScope = 0;
	
	/** The {@link Actions}. */
	private Actions actions = new Actions();
	
	/** 
	 * The identifier of the branch state in the state space subregion 
	 * we want to explore (null for exploring the whole state space
	 * starting from root). 
	 */
	private String identifierSubregion = null;
	
	/** 
	 * Constructor. 
	 */
	public RunnerParameters() {
		this.engineParameters = new EngineParameters();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param engineParameters an {@link EngineParameters} object.
	 *        The created object will be backed by {@code engineParameters}.
	 */
	public RunnerParameters(EngineParameters engineParameters) {
		this.engineParameters = engineParameters;
	}
	
	public EngineParameters getEngineParameters() {
		return this.engineParameters;
	}

	/**
	 * Sets the decision procedure to be used during symbolic execution.
	 * 
	 * @param decisionProcedure a {@link DecisionProcedureAlgorithms}.
	 */
	public void setDecisionProcedure(DecisionProcedureAlgorithms decisionProcedure) {
		this.engineParameters.setDecisionProcedure(decisionProcedure);
	}
	
	/**
	 * Gets the decision procedure.
	 * 
	 * @return a {@link DecisionProcedureAlgorithms}.
	 */
	public DecisionProcedureAlgorithms getDecisionProcedure() {
		return this.engineParameters.getDecisionProcedure();
	}
	
	public void setCalculator(Calculator calc) {
		this.engineParameters.setCalculator(calc);
	}

	public Calculator getCalculator() {
		return this.engineParameters.getCalculator();
	}

	/**
	 * Sets the state identification mode, i.e., how a state will be
	 * identified.
	 * 
	 * @param stateIdMode a {@link StateIdentificationMode}.
	 * @throws NullPointerException if {@code stateIdMode == null}.
	 */
	public void setStateIdentificationMode(StateIdentificationMode stateIdMode) {
		this.engineParameters.setStateIdentificationMode(stateIdMode);
	}
	
	/**
	 * Gets the state identification mode.
	 * 
	 * @return the {@link StateIdentificationMode} set by the
	 *         last call to {@link #setStateIdentificationMode(StateIdentificationMode)}.
	 */
	public StateIdentificationMode getStateIdentificationMode() {
		return this.engineParameters.getStateIdentificationMode();
	}
	
	/**
	 * Sets the breadth mode, i.e., how many branches 
	 * will be created during execution.
	 * 
	 * @param breadthMode a {@link BreadthMode}.
	 * @throws NullPointerException if {@code breadthMode == null}.
	 */
	public void setBreadthMode(BreadthMode breadthMode) {
		this.engineParameters.setBreadthMode(breadthMode);
	}
	
	/**
	 * Gets the breadth mode.
	 * 
	 * @return the {@link BreadthMode} set by the
	 *         last call to {@link #setBreadthMode(BreadthMode)}.
	 */
	public BreadthMode getBreadthMode() {
		return this.engineParameters.getBreadthMode();
	}

	/**
	 * Sets the initial state of the symbolic execution, and cancels the 
	 * effect of any previous call to {@link #addClasspath(String...)},
	 * {@link #setMethodSignature(String)}.
	 *  
	 * @param s a {@link State}.
	 */
	public void setInitialState(State s) { 
		this.engineParameters.setInitialState(s);
	}
	
	/**
	 * Gets the initial state of the symbolic execution (a safety copy).
	 * 
	 * @return the {@link State} set by the last call to 
	 *         {@link #setInitialState(State)} (possibly {@code null}).
	 */
	public State getInitialState() {
		return this.engineParameters.getInitialState();
	}

	/**
	 * Sets the symbolic execution's classpath; the 
	 * default classpath is {@code "."}.
	 * 
	 * @param paths a varargs of {@link String}, 
	 *        the paths to be added to the classpath.
	 */
	public void addClasspath(String... paths) { 
		this.engineParameters.addClasspath(paths);
	}
	
    /**
     * Clears the symbolic execution's classpath.
     */
	public void clearClasspath() {
	    this.engineParameters.clearClasspath();
	}

	/**
	 * Returns the symbolic execution's classpath.
	 * 
	 * @return a {@link Classpath} object. 
	 */
	public Classpath getClasspath() {
		return this.engineParameters.getClasspath();
	}

	/**
	 * Sets the signature of the method which must be symbolically executed.
	 * 
	 * @param className the name of the class containing the method.
	 * @param descriptor the descriptor of the method.
	 * @param methodName the name of the method. 
	 * @throws NullPointerException if any of the above parameters is {@code null}.
	 */
	public void setMethodSignature(String className, String descriptor, String methodName) { 
		this.engineParameters.setMethodSignature(className, descriptor, methodName); 
	}
	
	/**
	 * Gets the signature of the method which must be symbolically executed.
	 * 
	 * @return a {@link Signature}, or {@code null} if no method signature
	 *         has been provided.
	 */
	public Signature getMethodSignature() {
		return this.engineParameters.getMethodSignature();
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
		this.engineParameters.addExecutionObserver(className, type, observedVar, observer);
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
		this.engineParameters.addMetaOverridden(className, descriptor, methodName, metaDelegateClassName);
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
		this.engineParameters.addUninterpreted(className, descriptor, methodName, functionName);
	}
    
    /**
     * Adds a trigger method that fires when some references are resolved by
     * expansion.
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
     *                     the class of the trigger method.
     * @param triggerParametersSignature 
     *                     the types of the parameters of the trigger method.
     * @param triggerMethodName 
     *                     the name of the trigger method.
     * @param triggerParameter
     *                     the parameter to be passed to the trigger method. 
     */
	public void addExpandToTrigger(String toExpand, String originExp, String classAllowed, 
			String triggerClassName, String triggerParametersSignature, String triggerMethodName,
			String triggerParameter) {
		this.engineParameters.addExpandToTrigger(toExpand, originExp, classAllowed,
				triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter);
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
		this.engineParameters.addResolveAliasOriginTrigger(toResolve, originExp, pathAllowedExp, 
				triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter);
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
		this.engineParameters.addResolveAliasInstanceofTrigger(toResolve, originExp, classAllowed, 
				triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter);
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
		this.engineParameters.addResolveNullTrigger(toResolve,  originExp, triggerClassName, 
				triggerParametersSignature, triggerMethodName, triggerParameter);
	}

	/**
	 * Sets a timeout for execution.
	 * 
	 * @param time a {@code long}, the amount of time.
	 * @param timeUnit the {@link TimeUnit} of {@code long}.
	 */
	public void setTimeout(long time, TimeUnit timeUnit) { 
		this.timeout = timeUnit.toMillis(time);
	}

	/**
	 * Sets no time limit for execution. This is the 
	 * default behavior.
	 */
	public void setTimeoutUnlimited() { 
		this.timeout = 0;
	}
	
	/**
	 * Gets the timeout for execution.
	 * 
	 * @return a {@code long}, the timeout in milliseconds.
	 */
	public long getTimeout() {
	    return this.timeout;
	}
	
	/**
	 * Sets a limited heap scope for the objects of a given class. 
	 * The heap scope is the maximum number of objects of a given class 
	 * in the initial state's heap. If during the symbolic execution 
	 * the number of assumed objects of a given class is above the associated 
	 * heap scope, the exploration of the branch is interrupted.
	 * 
	 * @param className a {@link String}, the name of a class.
	 * @param heapScope an {@code int}, the heap scope associated to {@link className}.
	 */
	public void setHeapScope(String className, int heapScope) { 
		this.heapScopeStatic.put(className, heapScope); 
	}
	
	/**
	 * Sets a limited heap scope for the objects of a given class. 
	 * The scope is computed from the initial state: If there is no initial
	 * state this method has no effect, otherwise the heap scope specified
	 * with this method will override the scope for the same class
	 * that was previously (and that will be in the future) specified with 
	 * {@link #setHeapScope(String, int)}
	 * or {@link #setHeapScope(Map<String, Integer>)}
	 * 
	 * @param className a {@link String}, the name of a class.
	 * @param heapScope a {@link Function}{@code <}{@link State}{@code , }{@link Integer}{@code >}, 
	 *        the function that calculates the heap scope associated to {@link className} from 
	 *        the initial state.
	 * @see {@link #setHeapScope(String, int)} for a precise definition of heap scope.
	 */
	public void setHeapScopeComputed(String className, Function<State, Integer> heapScopeCalculator) { 
		this.heapScopeComputed.put(className, heapScopeCalculator); 
	}
	
	/**
	 * Sets a limited heap scope for the objects of a given list of classes. 
	 * It behaves as a reset of the effects of all the previous calls to 
	 * {@link #setHeapScopeComputed(String, Function<State, Integer>)}, followed by a sequence of calls to 
	 * {@link #setHeapScopeComputed(String, Function<State, Integer>)} for all the entries in the map.
	 * 
	 * @param className a {@link String}, the name of a class.
	 * @param heapScope a {@link Map}{@code <}{@link String}{@code , }{@link Function}{@code <}{@link State}{@code , }{@link Integer}{@code >>}, 
	 *        associating class names with a function that calculates the 
	 *        heap scope for the class from the initial state. All the
	 *        mappings in {@code heapScope} are copied for safety.
	 * @see {@link #setHeapScope(String, int)} for a precise definition of heap scope.
	 */
	public void setHeapScopeComputed(Map<String, Function<State, Integer>> heapScope) {
		this.heapScopeComputed.clear();
		this.heapScopeComputed.putAll(heapScope);
	}

	/**
	 * Sets an unlimited heap scope for the objects of a given class. 
	 * 
	 * @param className a {@link String}, the name of the class.
	 * @see {@link #setHeapScope(String, int)} for a precise definition of heap scope.
	 */
	public void setHeapScopeUnlimited(String className) { 
		this.heapScopeStatic.remove(className); 
		this.heapScopeComputed.remove(className); 
	}

	/**
	 * Sets an unlimited heap scope for all the classes; this is the default 
	 * behaviour. 
	 * 
	 * @see {@link #setHeapScope(String, int)} for a precise definition of heap scope.
	 */
	public void setHeapScopeUnlimited() { 
		this.heapScopeStatic.clear(); 
		this.heapScopeComputed.clear();
	}
	
	/**
	 * Gets the heap scope for the objects of a given class. 
	 * 
	 * @return heapScope a {@link Map}{@code <}{@link String}{@code , }{@link Integer}{@code >}, 
	 *        associating class names with their respective heap scopes.
	 *        The scopes are possibly computed by applying the functions set
	 *        with the calls to  {@link #setHeapScopeComputed(String, Function)}
	 *        or {@link #setHeapScopeComputed(Map)}.
	 *        If a class is not present in the map, its scope is unlimited.
	 *        Each time this method is invoked it creates and returns a new {@link Map}.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Integer> getHeapScope() {
		final Map<String, Integer> retVal = (Map<String, Integer>) this.heapScopeStatic.clone();
		final State initialState = getInitialState();
		if (initialState != null) {
			for (Map.Entry<String, Function<State, Integer>> entry : this.heapScopeComputed.entrySet()) {
				final String className = entry.getKey();
				final Function<State, Integer> heapScopeCalculator = entry.getValue();
				retVal.put(className, heapScopeCalculator.apply(initialState));
			}
		}
		return retVal;
	}
	
	/**
	 * Sets a limited depth scope. 
	 * The depth of a state is the number of branches above it. If 
	 * a state has a depth greater than the depth scope the exploration 
	 * of the branch it belongs is interrupted.
	 * 
	 * @param depthScope an {@code int}, the depth scope.
	 */
	public void setDepthScope(int depthScope) { 
		this.depthScope = depthScope; 
	}

	/**
	 * Sets an unlimited depth scope; this is the default behaviour.
	 * 
	 * @see {@link #setDepthScope(int)}
	 */
	public void setDepthScopeUnlimited() { 
		this.depthScope = 0; 
	}
	
	/**
	 * Gets the depth scope.
	 * 
	 * @return an {@code int}, the depth scope or {@code 0}
	 *         for unlimited depth scope.
	 */
	public int getDepthScope() {
	    return this.depthScope;
	}
	
	/**
	 * Sets a limited count scope. 
	 * If a state has a number of predecessor states greater than the 
	 * count scope the exploration of the branch it belongs is interrupted.
	 * 
	 * @param countScope an {@code int}, the count scope.
	 */
	public void setCountScope(int countScope) { 
		this.countScope = countScope; 
	}
	
	/**
	 * Sets an unlimited count scope; this is the default behaviour.
	 */
	public void setCountScopeUnlimited() { 
		this.countScope = 0; 
	}
	
    /**
     * Gets the count scope.
     * 
     * @return an {@code int}, the count scope or {@code 0}
     *         for unlimited count scope.
     */
	public int getCountScope() {
	    return this.countScope;
	}
	
	/**
	 * Sets the actions to be performed while running.
	 * 
	 * @param actions the {@link Actions} to be performed.
	 * @throws NullPointerException if {@code actions == null}.
	 */
	public void setActions(Actions actions) {
		if (actions == null) {
			throw new NullPointerException();
		}
		this.actions = actions;
	}
	
	/**
	 * Sets the actions to be performed while running to
	 * the actions that do nothing. This is the default 
	 * behavior.
	 */
	public void setActionsNothing() {
		this.actions = new Actions();
	}
	
	/**
	 * Gets the actions to be performed while running.
	 */
	public Actions getActions() {
	    return this.actions;
	}
	
	/**
	 * Sets the identifier of the initial state in the state space subregion 
	 * to be explored.
	 * 
	 * @param identifierSubregion a {@link String}, the subregion identifier.
	 *        For example, if {@code identifierSubregion == ".1.2.1"} the 
	 *        execution will explore only the traces whose identifier starts
	 *        with .1.2.1 (i.e., 1.2.1.1.2, 1.2.1.3.2.1.4, and not 1.2.2.1.2).
	 * @throws NullPointerException if {@code identifierSubregion == null}.
	 */
	public void setIdentifierSubregion(String identifierSubregion) {
		if (identifierSubregion == null) {
			throw new NullPointerException();
		}
		this.identifierSubregion = identifierSubregion;
	}
	
	/**
	 * Instructs to explore the whole state space starting
	 * from the root state. This is the default behavior.
	 */
	public void setIdentifierSubregionRoot() {
		this.identifierSubregion = null;
	}
	
	/**
	 * Gets the identifier of the initial state in the state space subregion 
     * to be explored.
     * 
	 * @return a {@code String}, or {@code null} if the state space
	 *         must be explored starting from root.
	 */
	public String getIdentifierSubregion() {
	    return this.identifierSubregion;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public RunnerParameters clone() {
		final RunnerParameters o;
		try {
			o = (RunnerParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		o.engineParameters = this.engineParameters.clone(); 
		if (this.heapScopeStatic != null) {
			o.heapScopeStatic = (HashMap<String, Integer>) this.heapScopeStatic.clone();
		}
		if (this.heapScopeComputed!= null) {
			o.heapScopeComputed = (HashMap<String, Function<State, Integer>>) this.heapScopeComputed.clone();
		}
		//actions cannot be cloned, as they come with a context. Beware!
		return o;
	}
}
