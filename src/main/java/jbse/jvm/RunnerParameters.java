package jbse.jvm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.jvm.EngineParameters.BreadthMode;
import jbse.jvm.EngineParameters.StateIdentificationMode;
import jbse.jvm.Runner.Actions;
import jbse.mem.State;
import jbse.rules.TriggerRulesRepo;
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

    /**
     * Returns the wrapped {@link EngineParameters} object.
     * 
     * @return the {@link EngineParameters} object that backs
     *         this {@link RunnerParameters}.
     */
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
     * Adds an {@link ExecutionObserver} performing additional
     * actions when a field changes its value.
     * 
     * @param fldClassName the name of the class where the field
     *        resides.
     * @param fldType the type of the field.
     * @param fldName the name of the field.
     * @param observer an {@link ExecutionObserver}. It will be 
     *        notified whenever the field {@code fldName} of 
     *        any instance of {@code fldClassName} is modified.
     */ 
    public void addExecutionObserver(String fldClassName, String fldType, String fldName, ExecutionObserver observer) {
        this.engineParameters.addExecutionObserver(fldClassName, fldType, fldName, observer);
    }

    /**
     * Clears the {@link ExecutionObserver}s.
     */
    public void clearExecutionObservers() {
    	this.engineParameters.clearExecutionObservers();
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
        return this.engineParameters.getObservedFields();
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
        return this.engineParameters.getObservers();
    }

    /**
     * Sets the starting state of the symbolic execution, and cancels the 
     * effect of any previous call to {@link #setJavaHome(String) setJavaHome}, 
     * {@link #addExtClasspath(String...) addExtClasspath}, 
     * {@link #addUserClasspath(String...) addUserClasspath}, 
     * and {@link #setMethodSignature(String) setMethodSignature}.
     *  
     * @param s a {@link State}.
     */
    public void setStartingState(State s) { 
        this.engineParameters.setStartingState(s);
    }

    /**
     * Gets the starting state of the symbolic execution (a safety copy).
     * 
     * @return the {@link State} set by the last call to 
     *         {@link #setStartingState(State)} (possibly {@code null}).
     */
    public State getStartingState() {
        return this.engineParameters.getStartingState();
    }

    /**
     * Sets whether the bootstrap classloader should also be used to 
     * load the classes defined by the extensions and application classloaders.
     * This deviates a bit from the Java Virtual Machine Specification, 
     * but ensures in practice a faster loading of classes than by
     * the specification mechanism of invoking the {@link ClassLoader#loadClass(String) ClassLoader.loadClass}
     * method. By default it is set to {@code true}. Also cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param bypassStandardLoading a {@code boolean}.
     */
    public void setBypassStandardLoading(boolean bypassStandardLoading) {
        this.engineParameters.setBypassStandardLoading(bypassStandardLoading);
    }
    
    /**
     * Gets whether the bootstrap classloader should also be used to 
     * load the classes defined by the extensions and application classloaders.
     * 
     * @return a {@code boolean}.
     */
    public boolean getBypassStandardLoading() {
        return this.engineParameters.getBypassStandardLoading();
    }

    /**
     * Sets the {@link Calculator}.
     * 
     * @param calc a {@link Calculator}.
     * @throws NullPointerException if {@code calc == null}.
     */
    public void setCalculator(Calculator calc) {
        this.engineParameters.setCalculator(calc);
    }

    /**
     * Gets the {@link Calculator}.
     * 
     * @return a {@link Calculator}
     */
    public Calculator getCalculator() {
        return this.engineParameters.getCalculator();
    }

    /**
     * Sets the path of the JBSE library, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param jbseLibPath a {@link String}.
     * @throws NullPointerException if {@code jbseLibPath == null}.
     */
    public void setJBSELibPath(String jbseLibPath) {
    	this.engineParameters.setJBSELibPath(jbseLibPath);
    }
    
    /**
     * Sets the path of the JBSE library, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param jbseLibPath a {@link Path}.
     * @throws NullPointerException if {@code jbseLibPath == null}.
     */
    public void setJBSELibPath(Path jbseLibPath) {
    	this.engineParameters.setJBSELibPath(jbseLibPath);
    }

    /**
     * Gets the path of the JBSE library.
     * 
     * @return a {@link Path}, the path of the JBSE library.
     */
    public Path getJBSELibPath() {
    	return this.engineParameters.getJBSELibPath();
    }

    /**
     * Sets the Java home, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param javaHome a {@link String}.
     * @throws NullPointerException if {@code javaHome == null}.
     */
    public void setJavaHome(String javaHome) {
        this.engineParameters.setJavaHome(javaHome);
    }
    
    /**
     * Sets the Java home, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param javaHome a {@link Path}.
     * @throws NullPointerException if {@code javaHome == null}.
     */
    public void setJavaHome(Path javaHome) {
        this.engineParameters.setJavaHome(javaHome);
    }

    /**
     * Brings the Java home back to the default,
     * i.e., the same bootstrap path of the JVM that
     * executes JBSE, as returned by the system property
     * {@code java.home}. Also cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     */
    public void setDefaultJavaHome() {
        this.engineParameters.setDefaultJavaHome();
    }

    /**
     * Gets the Java home.
     * 
     * @return a {@link Path}, the Java home.
     */
    public Path getJavaHome() {
        return this.engineParameters.getJavaHome();
    }

    /**
     * Adds paths to the extensions classpath, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param paths a varargs of {@link String}s, 
     *        the paths to be added to the extensions 
     *        classpath.
     * @throws NullPointerException if {@code paths == null}.
     */
    public void addExtClasspath(String... paths) {
        this.engineParameters.addExtClasspath(paths);
    }
    
    /**
     * Adds paths to the extensions classpath, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param paths a varargs of {@link Path}s, 
     *        the paths to be added to the extensions 
     *        classpath.
     * @throws NullPointerException if {@code paths == null}.
     */
    public void addExtClasspath(Path... paths) { 
        this.engineParameters.addExtClasspath(paths);
    }

    /**
     * Sets the extensions classpath to
     * no path.
     */
    public void clearExtClasspath() {
        this.engineParameters.clearExtClasspath();
    }
    
    /**
     * Brings the extensions classpath back to the default,
     * i.e., the same extensions path of the JVM that
     * executes JBSE, as returned by the system property
     * {@code java.ext.dirs}. Also cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     */
    public void setDefaultExtClasspath() {
        this.engineParameters.setDefaultExtClasspath();
    }
    
    /**
     * Adds paths to the user classpath, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param paths a varargs of {@link String}s, 
     *        the paths to be added to the user 
     *        classpath.
     * @throws NullPointerException if {@code paths == null}.
     */
    public void addUserClasspath(String... paths) { 
        this.engineParameters.addUserClasspath(paths);
    }

    /**
     * Adds paths to the user classpath, and cancels the effect 
     * of any previous call to {@link #setStartingState(State)}.
     * 
     * @param paths a varargs of {@link Path}s, 
     *        the paths to be added to the user 
     *        classpath.
     * @throws NullPointerException if {@code paths == null}.
     */
    public void addUserClasspath(Path... paths) { 
        this.engineParameters.addUserClasspath(paths);
    }

    /**
     * Brings the user classpath back to the default,
     * i.e., no user path.
     */
    public void clearUserClasspath() {
        this.engineParameters.clearUserClasspath();
    }

    /**
     * Builds the classpath.
     * 
     * @return a {@link Classpath} object. 
     * @throws IOException if an I/O error occurs while scanning the classpath.
     */
    public Classpath getClasspath() throws IOException  {
        return this.engineParameters.getClasspath();
    }

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
        return this.engineParameters.getTriggerRulesRepo();
    }

    /**
     * Adds a regular expression pattern of class names whose 
     * state did not change after their class initialization, 
     * up to the beginning of symbolic execution. Upon their 
     * first access their class initializers will be executed.
     *  
     * @param classPattern a {@link String}. The {@code null} 
     *        value will be ignored.
     */
    public void addClassInvariantAfterInitializationPattern(String classPattern) {
    	this.engineParameters.addClassInvariantAfterInitializationPattern(classPattern);
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
        return this.engineParameters.getExpansionBackdoor();
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
     * Clears the specifications of the meta-level implementations 
     * of methods.
     */
    public void clearMetaOverridden() {
        this.engineParameters.clearMetaOverridden();
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
        return this.engineParameters.getMetaOverridden();
    }

    /**
     * Specifies that a method must be treated as an uninterpreted pure
     * function, rather than executed. In the case all the parameters are
     * constant, the method is executed metacircularly.
     * 
     * @param methodClassName the name of the class containing the method.
     * @param methodDescriptor the descriptor of the method.
     * @param methodName the name of the method.
     * @throws NullPointerException if any of the above parameters is {@code null}.
     */
    public void addUninterpreted(String methodClassName, String methodDescriptor, String methodName) {
        this.engineParameters.addUninterpreted(methodClassName, methodDescriptor, methodName);
    }

    /**
     * Specifies that a set of methods must be treated as an uninterpreted pure
     * function, rather than executed. In the case all the parameters are
     * constant, the methods are executed metacircularly.
     * 
     * @param patternMethodClassName a regular expression pattern for the names 
     *        of the classes containing the methods.
     * @param patternMethodDescriptor a regular expression pattern for the descriptors
     *        of the methods.
     * @param patternMethodName a regular expression pattern for the names of the methods.
     * @throws NullPointerException if any of the above parameters is {@code null}.
     */
    public void addUninterpretedPattern(String patternMethodClassName, String patternMethodDescriptor, String patternMethodName) {
    	this.engineParameters.addUninterpretedPattern(patternMethodClassName, patternMethodDescriptor, patternMethodName);
    }

    /**
     * Clears the methods set with {@link #addUninterpreted(String, String, String) addUninterpreted} 
     * that must be treated as uninterpreted pure functions.
     */
    public void clearUninterpreted() {
        this.engineParameters.clearUninterpreted();
    }

    /**
     * Clears the methods set with {@link #addUninterpretedPattern(String, String, String) addUninterpretedPattern} 
     * that must be treated as uninterpreted pure functions.
     */
    public void clearUninterpretedPattern() {
    	this.engineParameters.clearUninterpretedPattern();
    }

    /**
     * Returns the methods that must be treated as
     * uninterpreted pure functions.
     * 
     * @return A {@link List}{@code <}{@link String}{@code []>}, 
     *         where each array is a triple (method class name, 
     *         method parameters, method name).
     */
    public List<String[]> getUninterpreted() {
    	return this.engineParameters.getUninterpreted();
    }

    /**
     * Returns the method patterns that must be treated as
     * uninterpreted pure functions.
     * 
     * @return A {@link List}{@code <}{@link String}{@code []>}, 
     *         where each array is a triple (pattern of method 
     *         class name, pattern of method parameters, pattern 
     *         of method names).
     */
    public List<String[]> getUninterpretedPattern() {
    	return this.engineParameters.getUninterpretedPattern();
    }

    /**
     * Sets the signature of the method which must be symbolically executed, 
     * and cancels the effect of any previous call to {@link #setStartingState(State)}.
     * 
     * @param className the name of the class containing the method.
     * @param descriptor the descriptor of the method.
     * @param name the name of the method. 
     * @throws NullPointerException if any of the above parameters is {@code null}.
     */
    public void setMethodSignature(String className, String descriptor, String name) { 
        this.engineParameters.setMethodSignature(className, descriptor, name); 
    }

    /**
     * Sets the signature of the method which must be symbolically executed,
     * and cancels the effect of any previous call to {@link #setStartingState(State)}.
     *
     * @param signature the signature of the method to execute symbolically.
     * @throws NullPointerException if any of the above parameters is {@code null}.
     */
    public void setMethodSignature(Signature signature) {
        this.engineParameters.setMethodSignature(signature);
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
     * Sets the maximum length an array must have to be 
     * granted simple representation.
     * 
     * @param maxSimpleArrayLength an {@code int}.
     */
    public void setMaxSimpleArrayLength(int maxSimpleArrayLength) {
        this.engineParameters.setMaxSimpleArrayLength(maxSimpleArrayLength);
    }

    /**
     * Returns the maximum length an array must have to be 
     * granted simple representation.
     * 
     * @param maxSimpleArrayLength an {@code int}.
     */
    public int getMaxSimpleArrayLength() {
        return this.engineParameters.getMaxSimpleArrayLength();
    }
    
    /**
     * Sets the maximum heap size, expressed as the 
     * maximum number of objects in the heap. If 
     * during symbolic execution the heap becomes
     * bigger than this treshold, JBSE raises 
     * an {@link OutOfMemoryError}.
     * 
     * @param maxHeapSize a {@code long}.
     */
    public void setMaxHeapSize(long maxHeapSize) {
        this.engineParameters.setMaxHeapSize(maxHeapSize);
    }
    
    /**
     * Returns the maximum heap size, expressed
     * as the maximum number of objects in the heap.
     * 
     * @return a {@code long}.
     */
    public long getMaxHeapSize() {
        return this.engineParameters.getMaxHeapSize();
    }
    
    /**
     * Sets whether the classes created during
     * the pre-initialization phase shall be (pedantically)
     * considered symbolic.
     * 
     * @param makePreInitClassesSymbolic a {@code boolean}.
     *        If {@code true} all the classes created during
     *        the pre-inizialization phase will be made 
     *        symbolic.
     */
    public void setMakePreInitClassesSymbolic(boolean makePreInitClassesSymbolic) {
    	this.engineParameters.setMakePreInitClassesSymbolic(makePreInitClassesSymbolic);
    }
    
    /**
     * Returns whether the classes created during
     * the pre-initialization phase shall be (pedantically)
     * considered symbolic.
     * 
     * @return a {@code boolean}.
     */
    public boolean getMakePreInitClassesSymbolic() {
    	return this.engineParameters.getMakePreInitClassesSymbolic();
    }
    
    /**
     * Sets whether, instead of the JDK implementation of 
     * {@code java.util.HashMap}, a model class must be used
     * during symbolic execution.
     * 
     * @param useHashMapModel a {@code boolean}. If {@code true} all
     *        the hash maps will be replaced by a model class that
     *        is more symbolic-execution-friendly than {@code java.util.HashMap}.
     */
    public void setUseHashMapModel(boolean useHashMapModel) {
    	this.engineParameters.setUseHashMapModel(useHashMapModel);
    }
    
    /**
     * Returns whether, instead of the JDK implementation of 
     * {@code java.util.HashMap}, a model class must be used
     * during symbolic execution.
     * 
     * @return a {@code boolean}.
     */
    public boolean getUseHashMapModel() {
    	return this.engineParameters.getUseHashMapModel();
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
        final State startingState = getStartingState();
        if (startingState != null) {
            for (Map.Entry<String, Function<State, Integer>> entry : this.heapScopeComputed.entrySet()) {
                final String className = entry.getKey();
                final Function<State, Integer> heapScopeCalculator = entry.getValue();
                retVal.put(className, heapScopeCalculator.apply(startingState));
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
     *        For example, if {@code identifierSubregion.equals(".1.2.1")} the 
     *        execution will explore only the paths whose identifier starts
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
