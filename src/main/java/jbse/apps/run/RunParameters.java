package jbse.apps.run;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureAlwSat;
import jbse.dec.DecisionProcedureClassInit;
import jbse.jvm.EngineParameters;
import jbse.jvm.ExecutionObserver;
import jbse.jvm.RunnerParameters;
import jbse.jvm.EngineParameters.BreadthMode;
import jbse.jvm.EngineParameters.StateIdentificationMode;
import jbse.mem.State;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.Rewriter;
import jbse.rules.ClassInitRulesRepo;
import jbse.rules.LICSRulesRepo;
import jbse.val.ReferenceSymbolic;

/**
 * Encapsulates a {@link Run}'s parameters. The most
 * important ones are:
 * 
 * <ul>
 * <li>Some of the {@link RunnerParameters};
 * <li>The source path (. by default);</li>
 * <li>A {@link DecisionProcedureType}, specifying 
 * which decision procedure must be used;</li>
 * <li>The output file to copy the output produced to 
 * stdout and stderr is copied (by default, no output 
 * file is created and the output is just sent to 
 * stdout and stderr);</li>
 * <li>A {@link TextMode}, indicating how the rows on the 
 * output file must be terminated; by default, the 
 * platform's line termination mode is used. This parameter 
 * is relevant only when an output file is specified;</li>
 * <li>An {@link InteractionMode}, indicating the amount of 
 * required user interaction (none by default);</li>
 * <li>A {@link StepShowMode}, indicating which states
 * of the symbolic execution must be displayed;</li>
 * <li>A {@link StateFormatMode}, indicating how a 
 * symbolic execution state is displayed;</li>
 * <li>The visualization status of the interactions 
 * between the decision procedure and the runner; by default, 
 * nothing is showed, but it is possible to dump a high-level 
 * description of the exchanged messages on the output;</li>
 * <li>The classes that must be assumed to be not yet initialized 
 * at the beginning of symbolic execution (none by default);</li>
 * <li>The LICS rules and/or conservative repOk methods to be 
 * used for filtering states that violate assumed 
 * preconditions/invariants (none by default);</li>
 * <li>Whether the symbolic execution should be guided by a 
 * concrete execution or not, and in the positive case the 
 * signature of the driver method for the concrete execution;</li>
 * <li>The signatures of the methods to be used for checking 
 * whether a leaf symbolic states is concretizable, i.e., is 
 * inhabited by a well-formed concrete state (none by default).
 * </ul> 
 * 
 * @author Pietro Braione
 */
public final class RunParameters implements Cloneable {
	/**
	 * Enumeration of the possible decision procedures.
	 * 
	 * @author Pietro Braione
	 */
	public static enum DecisionProcedureType {
		/** Does not use a decision procedure, all formulas will be considered satisfiable. */
		ALL_SAT,

		/** Uses Z3. */
		Z3,
		
		/** Uses CVC4. */
		CVC4
	}
	
	/**
	 * Enumeration of the possible guidance decision procedures.
	 * 
	 * @author Pietro Braione
	 *
	 */
	public static enum GuidanceType {
	    /** Use JBSE for the concrete execution. */
	    JBSE,
	    
	    /** Use the platform JVM through JDI for the concrete execution. */
	    JDI
	}

	/**
	 * A Strategy for creating {@link DecisionProcedure}s. 
	 * The strategy receives as inputs the necessary dependencies
	 * to inject in it, an must return the decision procedure
	 * object.
	 * 
	 * @author Pietro Braione
	 *
	 */
	@FunctionalInterface
	public interface DecisionProcedureCreationStrategy {
		/**
		 * Creates a {@link DecisionProcedure}.
		 * 
		 * @param core a previously built {@link DecisionProcedure}.
		 * @param calc a {@link CalculatorRewriting}.
		 * @return a new {@link DecisionProcedure} that (possibly) has {@code core} 
		 *         as Decorator component, or next decision procedure in the 
		 *         Chain Of Responsibility, and (possibly) uses {@code calc} for 
		 *         calculations and simplifications.
		 */
		DecisionProcedure createAndWrap(DecisionProcedure core, CalculatorRewriting calc)
		throws CannotBuildDecisionProcedureException;
	}
	
    /**
	 * Enumeration of the possible temporal granularity of output, determining 
	 * which states are displayed and which are not during the execution.
	 * 
	 * @author Pietro Braione
	 */
	public enum StepShowMode {
		/** Displays all the states. */
		ALL, 
		
		/** Displays states at beginning of source code lines. */
		SOURCE,
		
		/** Displays states at entry of methods. */
		METHOD,	

		/** 
		 * Displays the root state, all the branch states and all the
		 * leaves, i.e., the stuck states.
		 */
		ROOT_BRANCHES_LEAVES,
		
		/**
		 * For each leaf state, displays the root state 
		 * refined on the leaf's path condition, followed by the 
		 * leaf itself.
		 */
		SUMMARIES,
	
		/** Displays only the leaves. */
		LEAVES,
		
		/** Displays nothing. */
		NONE
	}
	
	/**
	 * The trace (leaf) types.
	 * 
	 * @author Pietro Braione
	 */
	public enum TraceTypes {
		/** 
		 * A safe leaf, i.e., the final state of a 
		 * trace that does not violate any assertion
		 * or assumption. 
		 */
		SAFE,

		/** 
		 * An unsafe leaf, i.e., the final state of a 
		 * trace that violates an assertion. 
		 */
		UNSAFE,

		/** 
		 * A leaf that exhausts a bound.
		 */
		OUT_OF_SCOPE,
		
		/**
		 * An unmanageable leaf, i.e., the final 
		 * state of a trace that cannot be executed
		 * because the symbolic executor is unable
		 * to do that.
		 */
		UNMANAGEABLE,
		
		/**
		 * A contradictory leaf, i.e, the final 
		 * state of a trace that violates an 
		 * assumption.
		 */
		CONTRADICTORY
	}

	/**
	 * Enumeration of the possible text file line termination 
	 * modes.
	 * 
	 * @author Pietro Braione
	 */
	public enum TextMode {
		/** Separates the lines of text with the default platform separator. */
		PLATFORM,
		
		/** Separates the lines of text with the sequence of characters "\r\n". */ 
		WINDOWS, 
		
		/** Separates the lines of text with the character '\n'. */ 
		UNIX 
	}

	/**
	 * Enumeration of the possible interaction mode of a runner 
	 * session.
	 * @author Pietro Braione
	 */
	public enum InteractionMode { 
		/** 
		 * Does not prompt the user, exploring all paths in the 
		 * case it is unable to decide a branch. 
		 */
		NO_INTERACTION,
		
		/** 
		 * Prompts the user at every step of the symbolic computation, 
		 * before each backtrack point and at branching points which
		 * is unable to decide.
		 */
		STEP_BY_STEP, 
	
		/** 
		 * Prompts the user before each backtrack point and at 
		 * branching points which is unable to decide.
		 */
		PROMPT_BACKTRACK, 
		
		/** 
		 * Prompts the user at branching points which is unable 
		 * to decide. 
		 */
		ONLY_BRANCH_DECISION
	}

	/**
	 * Enumeration of the possible output display formats.
	 * 
	 * @author Pietro Braione
	 */
	public enum StateFormatMode {
		/** Displays the states textually (with indentation). */ 
		FULLTEXT,
		
		/** Displays the states as DOT graphs. */
		GRAPHVIZ,
		
		/** 
		 * Displays just a trace of the traversed states in the 
		 * format state id / state sequence number / 
		 * method signature / source row / program counter. 
		 */
		TRACE,
		
		/**
		 * Displays a JUnit class containing a suite that 
		 * covers all the symbolic states according to the
		 * step show mode.  
		 */
		JUNIT_TEST
	}

	/** The runner parameters. */
	private RunnerParameters runnerParameters;

	/** The {@link Class}es of all the rewriters to be applied to terms (order matters). */
	private ArrayList<Class<? extends Rewriter>> rewriterClasses = new ArrayList<>();
	
	/**
	 * The decision procedure to be used for deciding the 
	 * arithmetic conditions.
	 */
	private DecisionProcedureType decisionProcedureType = DecisionProcedureType.Z3;
	
	/** The decision procedure for guidance. */
	private GuidanceType guidanceType = GuidanceType.JBSE;
	
	/** The {@link Path} where the executable of the external decision procedure is. */
	private Path externalDecisionProcedurePath = null;

	/** 
	 * Whether the engine should use its sign analysis 
	 * decision support.
	 */
	private boolean doSignAnalysis = false;
	
	/** Whether the engine should do sign analysis before invoking the decision procedure. */
	private boolean doEqualityAnalysis = false;
	
	/** 
	 * Whether the engine should use the LICS decision procedure.
	 * Set to true by default because the LICS decision procedure
	 * also resolves class initialization. 
	 */
	private boolean useLICS = true;
	
	/** The {@link LICSRuleRepo}, containing all the LICS rules. */
	private LICSRulesRepo repoLICS = new LICSRulesRepo();
	
    /** The {@link ClassInitRulesRepo}, containing all the class initialization rules. */
    private ClassInitRulesRepo repoInit = new ClassInitRulesRepo();
    
	/** 
	 * Whether the engine should use the conservative 
	 * repOK decision procedure.
	 */
    private boolean useConservativeRepOks = false;
	
	/**
	 *  Associates classes with the name of their respective
	 *  conservative repOK methods. 
	 */
	private HashMap<String, String> conservativeRepOks = new HashMap<>();

	/** The heap scope for conservative repOK and concretization execution. */
	private HashMap<String, Function<State, Integer>> concretizationHeapScope = new HashMap<>();

	/** The depth scope for conservative repOK and concretization execution. */
	private int concretizationDepthScope = 0;

	/** The count scope for conservative repOK and concretization execution. */
	private int concretizationCountScope = 0;
	
	/** The {@link DecisionProcedureCreationStrategy} list. */
	private ArrayList<DecisionProcedureCreationStrategy> creationStrategies = new ArrayList<>();
	
	/** Should show output on console? */
	private boolean showOnConsole = true;

	/** The name of the output file. */
	private String outFileName = null;

	/** The text mode. */
	private TextMode textMode = TextMode.PLATFORM;

	/** The interaction mode. */
	private InteractionMode interactionMode = InteractionMode.NO_INTERACTION;

	/** The step show mode. */
	private StepShowMode stepShowMode = StepShowMode.ALL;

	/** The traces to show. */
	private EnumSet<TraceTypes> tracesToShow = EnumSet.allOf(TraceTypes.class);

	/** The format mode. */
	private StateFormatMode stateFormatMode = StateFormatMode.FULLTEXT;
	
	/** 
	 * Maximum stack depth to which we show code;
	 * if 0 we show at any depth (default).
	 */
	private int stackDepthShow = 0;
	
	/** 
	 * {@code true} iff at the end of traces the engine 
	 * must check if the trace can be concretized.
	 */
	private boolean doConcretization = false;
    
    /**
     *  Associates classes with the name of their respective
     *  concretization methods. 
     */
	private HashMap<String, String> concretizationMethods = new HashMap<>();

    /** 
     * {@code true} iff the tool info (welcome message, 
     * progress of tool initialization, final stats) 
     * must be logged. 
     */
	private boolean showInfo = true;

	/** 
	 * {@code true} iff the symbolic execution warnings 
	 * must be logged. 
	 */
	private boolean showWarnings = true;

	/** 
	 * {@code true} iff the interactions between the 
	 * runner and the decision procedure must be logged to 
	 * the output. 
	 */
	private boolean showDecisionProcedureInteraction = false;

	/**  
	 * The source code path, or {@code null} iff no path is specified, 
	 * which is the default. 
	 */
	private ArrayList<String> srcPath = new ArrayList<String>();

    /** Whether the symbolic execution is guided along a concrete one. */
    private boolean guided = false;
    
	/** The signature of the driver method when guided == true. */
    private Signature driverSignature = null;
	
	/**
	 * Constructor.
	 */
	public RunParameters() {
		this.runnerParameters = new RunnerParameters();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param runnerParameters a {@link RunnerParameters} object.
	 *        The created object will be backed by {@code runnerParameters}.
	 */
	public RunParameters(RunnerParameters runnerParameters) {
		this.runnerParameters = runnerParameters;
	}
	
	/**
	 * Gets the embedder {@link RunnerParameters} object.
	 * 
	 * @return the {@link RunnerParameters} that backs this 
	 *         {@link RunParameters} object.
	 */
	public RunnerParameters getRunnerParameters() {
		return this.runnerParameters;
	}

	/**
	 * Sets the state identification mode, i.e., how a state will be
	 * identified.
	 * 
	 * @param stateIdMode a {@link StateIdentificationMode}.
	 * @throws NullPointerException if {@code stateIdMode == null}.
	 */
	public void setStateIdentificationMode(StateIdentificationMode stateIdMode) {
		this.runnerParameters.setStateIdentificationMode(stateIdMode);
	}
	
	/**
	 * Sets the breadth mode, i.e., how many branches 
	 * will be created during execution.
	 * 
	 * @param breadthMode a {@link BreadthMode}.
	 * @throws NullPointerException if {@code breadthMode == null}.
	 */
	public void setBreadthMode(BreadthMode breadthMode) {
		this.runnerParameters.setBreadthMode(breadthMode);
	}

	/**
	 * Sets the symbolic execution's classpath; the 
	 * default classpath is {@code "."}.
	 * 
	 * @param paths a varargs of {@link String}, 
	 *        the paths to be added to the classpath.
	 */
	public void addClasspath(String... paths) { 
		this.runnerParameters.addClasspath(paths);
	}
	
    /**
     * Clears the symbolic execution's classpath.
     */
	public void clearClasspath() {
	    this.runnerParameters.clearClasspath();
	}

	/**
	 * Returns the symbolic execution's classpath.
	 * 
	 * @return a {@link Classpath} object. 
	 */
	public Classpath getClasspath() {
		return this.runnerParameters.getClasspath();
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
		this.runnerParameters.setMethodSignature(className, descriptor, methodName); 
	}
	
	/**
	 * Gets the signature of the method which must be symbolically executed.
	 * 
	 * @return a {@link Signature}, or {@code null} if no method signature
	 *         has been provided.
	 */
	public Signature getMethodSignature() {
		return this.runnerParameters.getMethodSignature();
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
		this.runnerParameters.addExecutionObserver(className, type, observedVar, observer);
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
		this.runnerParameters.addMetaOverridden(className, descriptor, methodName, metaDelegateClassName);
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
		this.runnerParameters.addUninterpreted(className, descriptor, methodName, functionName);
	}

	/**
	 * Sets a timeout for execution.
	 * 
	 * @param time a {@code long}, the amount of time.
	 * @param timeUnit the {@link TimeUnit} of {@code long}.
	 */
	public void setTimeout(long time, TimeUnit timeUnit) { 
		this.runnerParameters.setTimeout(time, timeUnit);
	}

	/**
	 * Sets no time limit for execution. This is the 
	 * default behavior.
	 */
	public void setTimeoutUnlimited() { 
		this.runnerParameters.setTimeoutUnlimited();
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
		this.runnerParameters.setHeapScope(className, heapScope); 
	}

	/**
	 * Sets an unlimited heap scope for the objects of a given class. 
	 * The heap scope is the maximum number of objects of a given class 
	 * in the initial state's heap. If during the symbolic execution 
	 * the number of assumed objects of a given class is above the associated 
	 * heap scope, the exploration of the branch is interrupted.
	 */
	public void setHeapScopeUnlimited(String className) { 
		this.runnerParameters.setHeapScopeUnlimited(className); 
	}

	/**
	 * Sets an unlimited heap scope for all the classes; this is the default 
	 * behaviour. 
	 * 
	 * @see {@link #setHeapScopeUnlimited(String)}
	 */
	public void setHeapScopeUnlimited() { 
		this.runnerParameters.setHeapScopeUnlimited(); 
	}
	
	/**
	 * Gets the heap scope for the objects of a given class. 
	 * 
	 * @return heapScope a {@link Map}{@code <}{@link String}{@code , }{@link Integer}{@code >}, 
	 *        associating class names with their respective heap scopes.
	 *        If a class is not present in the map, its scope is unlimited.
	 */
	public Map<String, Integer> getHeapScope() {
		return this.runnerParameters.getHeapScope();
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
		this.runnerParameters.setDepthScope(depthScope); 
	}

	/**
	 * Sets an unlimited depth scope; this is the default behaviour.
	 * 
	 * @see {@link #setDepthScope(int)}
	 */
	public void setDepthScopeUnlimited() { 
		this.runnerParameters.setDepthScopeUnlimited(); 
	}
	
	/**
	 * Sets a limited count scope. 
	 * If a state has a number of predecessor states greater than the 
	 * count scope the exploration of the branch it belongs is interrupted.
	 * 
	 * @param countScope an {@code int}, the count scope.
	 */
	public void setCountScope(int countScope) { 
		this.runnerParameters.setCountScope(countScope); 
	}
	
	/**
	 * Sets an unlimited count scope; this is the default behaviour.
	 */
	public void setCountScopeUnlimited() { 
		this.runnerParameters.setCountScopeUnlimited(); 
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
		this.runnerParameters.setIdentifierSubregion(identifierSubregion);
	}
	
	/**
	 * Instructs to explore the whole state space starting
	 * from the root state. This is the default behavior.
	 */
	public void setIdentifierSubregionRoot() {
		this.runnerParameters.setIdentifierSubregionRoot();
	}	
	
	/**
	 * Sets the classes of the rewriters to be applied to
	 * the terms created during symbolic execution.
	 * 
	 * @param rewriterClasses a vararg of {@link Class}{@code <? extends }
	 *        {@link Rewriter}{@code >}s.
	 *        They all must be either {@code null} (no rewriter)
	 *        or the class name of a class extending 
	 *        {@code Rewriter}, with a default constructor, and
	 *        in the classpath of the symbolic executor.
	 */
	@SafeVarargs
	public final void addRewriter(Class<? extends Rewriter>... rewriterClasses) {
		Collections.addAll(this.rewriterClasses, rewriterClasses);
	}
	
	/**
	 * Clears the classes of the rewriters to be applied to
     * the terms created during symbolic execution.
	 */
	public void clearRewriters() {
	    this.rewriterClasses.clear();
	}
	
	/**
	 * Returns the classes of the rewriters to be applied to
     * the terms created during symbolic execution.
     * 
	 * @return a {@link List}{@code <}{@link Class}{@code <? extends }
	 * {@link Rewriter}{@code >>}. It may contain {@code null}.
	 */
    public List<Class<? extends Rewriter>> getRewriters() {
	    return new ArrayList<>(this.rewriterClasses);
	}

	/**
	 * Sets the decision procedure type.
	 * 
	 * @param decisionProcedureType A {@link DecisionProcedureType} 
	 * representing the decision procedure.
	 * @throws NullPointerException if {@code decisionProcedureType == null}.
	 */
	public void setDecisionProcedureType(DecisionProcedureType decisionProcedureType) { 
		if (decisionProcedureType == null) {
			throw new NullPointerException();
		}
		this.decisionProcedureType = decisionProcedureType; 
	}
	
	/**
	 * Gets the decision procedure type.
	 * 
	 * @return a {@link DecisionProcedureType}.
	 */
	public DecisionProcedureType getDecisionProcedureType() {
		return this.decisionProcedureType;
	}

	/**
	 * Sets the pathname of the executable
	 * of the decision procedure (should match 
	 * {@link #setDecisionProcedureType(DecisionProcedureType)}).
	 * 
	 * @param externalDecisionProcedurePath a {@link String} containing a valid 
	 *        pathname for the decision procedure executable.
	 * @throws NullPointerException if {@code externalDecisionProcedurePath == null}.
	 * @throws InvalidPathException if {@code externalDecisionProcedurePath} is not
	 *         a valid path file name.
	 */
	public void setExternalDecisionProcedurePath(String externalDecisionProcedurePath) { 
		if (externalDecisionProcedurePath == null) {
			throw new NullPointerException();
		}
		this.externalDecisionProcedurePath = Paths.get(externalDecisionProcedurePath); 
	}
	
	/**
	 * Gets the pathname of the executable
	 * of the decision procedure set with 
	 * {@link #setExternalDecisionProcedurePath(String)}.
	 * 
	 * @return a nonnull {@link String}.
	 */
	public Path getExternalDecisionProcedurePath() {
		return this.externalDecisionProcedurePath;
	}
    
    /**
     * Adds a creation strategy to the strategies 
     * for creating the {@link DecisionProcedure}.
     * 
     * @param creationStrategy a {@link DecisionProcedureCreationStrategy}.
     * @throws NullPointerException if {@code creationStrategy == null}.
     */
    public void addDecisionProcedureCreationStrategy(DecisionProcedureCreationStrategy creationStrategy) {
        if (creationStrategy == null) {
            throw new NullPointerException();
        }
        this.creationStrategies.add(creationStrategy);
    }
    
    /**
     * Sets the creation strategy for the {@link DecisionProcedure} 
     * to plain decoration with
     * {@link DecisionProcedureAlgorithms}. This is the default.
     */
    public void clearDecisionProcedureCreationStrategies() {
        this.creationStrategies.clear();
    }
    
    /**
     * Returns all the strategies for creating the {@link DecisionProcedure},
     * in their order of addition.
     * 
     * @return a {@link List}{@code <}{@link DecisionProcedureCreationStrategy}{@code >}.
     */
    public List<DecisionProcedureCreationStrategy> getDecisionProcedureCreationStrategies() {
        return new ArrayList<>(this.creationStrategies);
    }
	
	/**
	 * Sets whether the engine should perform sign analysis
	 * for deciding inequations before invoking the decision procedure
	 * set with {@link #setDecisionProcedureType(DecisionProcedureType)}.
	 * 
	 * @param doSignAnalysis {@code true} iff the engine must do sign analysis.
	 */
	public void setDoSignAnalysis(boolean doSignAnalysis) {
		this.doSignAnalysis = doSignAnalysis;
	}
	
	/**
	 * Gets whether the engine should perform sign analysis
     * for deciding inequations.
	 * 
	 * @return {@code true} iff the engine must do sign analysis.
	 */
	public boolean getDoSignAnalysis() {
	    return this.doSignAnalysis;
	}
	
	/**
	 * Sets whether the engine should decide equality with a
	 * simple closure algorithm. 
	 * 
	 * @param doEqualityAnalysis {@code true} iff the engine must decide equalities.
	 */
	public void setDoEqualityAnalysis(boolean doEqualityAnalysis) {
		this.doEqualityAnalysis = doEqualityAnalysis;
	}

	/**
	 * Gets whether the engine should decide equality.
	 * 
	 * @return {@code true} iff the engine must decide equalities.
	 */
    public boolean getDoEqualityAnalysis() {
        return this.doEqualityAnalysis;
    }
    
	/**
	 * Sets whether the engine shall invoke or not the conservative
	 * repOk methods at every heap expansion. By default they are
	 * not invoked.
	 * 
	 * @param useConservativeRepOks {@code true} iff conservative
	 * repOk methods are invoked.
	 */
	public void setUseConservativeRepOks(boolean useConservativeRepOks) {
		this.useConservativeRepOks = useConservativeRepOks;
	}
	
	/**
	 * Returns whether the engine shall invoke or not the conservative
     * repOk methods at every heap expansion.
	 * 
	 * @return {@code true} iff conservative repOk methods are invoked.
	 */
	public boolean getUseConservativeRepOks() {
	    return this.useConservativeRepOks;
	}

	/**
	 * Specifies the conservative repOK method of a class.
	 * 
     * @param className the name of a class.
     * @param methodName the name of the conservative repOK method 
     *        contained in the class. It must be a parameterless
     *        nonnative instance method returning a boolean and it 
     *        must be defined in the class (i.e., it may not be
     *        inherited).
	 */
	public void addConservativeRepOk(String className, String methodName) {
	    this.conservativeRepOks.put(className, methodName);
	}
	
	/**
	 * Clears the conservative repOK methods of classes.
	 */
	public void clearConservativeRepOks() {
	    this.conservativeRepOks.clear();
	}
	
	/**
	 * Gets the conservative repOK methods of classes.
	 * 
	 * @return a {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}
	 *         mapping class names with the name of their respective conservative
	 *         repOK methods.
	 */
	public Map<String, String> getConservativeRepOks() {
	    return new HashMap<>(this.conservativeRepOks);
	}
	
	//TODO static (noncomputed) concretization heap scope

	/**
	 * Sets a limited heap scope for the objects of a given class
	 * during the symbolic execution of the concretization methods. 
	 * The heap scope is the maximum number of objects of a given class 
	 * in the initial state's heap. If during the symbolic execution 
	 * the number of assumed objects of a given class is above the associated 
	 * heap scope, the exploration of the branch is interrupted.
	 * 
	 * @param className a {@link String}, the name of a class.
	 * @param heapScopeCalculator a {@link Function}{@code <}{@link State}{@code , }{@link Integer}{@code >}, 
	 *        that calculates the heap scope associated to {@link className} from the initial
	 *        state.
	 */
	public void setConcretizationHeapScope(String className, Function<State, Integer> heapScopeCalculator) { 
		this.concretizationHeapScope.put(className, heapScopeCalculator); 
	}

	/**
	 * Sets an unlimited heap scope for the objects of a given class
	 * during the symbolic execution of the concretization methods. 
	 * The heap scope is the maximum number of objects of a given class 
	 * in the initial state's heap. If during the symbolic execution 
	 * the number of assumed objects of a given class is above the associated 
	 * heap scope, the exploration of the branch is interrupted.
	 */
	public void setConcretizationHeapScopeUnlimited(String className) { 
		this.concretizationHeapScope.remove(className); 
	}

	/**
	 * Sets an unlimited heap scope for all the classes during the 
	 * symbolic execution of the concretization methods; this is 
	 * the default behaviour. 
	 * The heap scope is the maximum number of objects of a given class 
	 * in the initial state's heap. If during the symbolic execution 
	 * the number of assumed objects of a given class is above the associated 
	 * heap scope, the exploration of the branch is interrupted.
	 */
	public void setConcretizationHeapScopeUnlimited() { 
		this.concretizationHeapScope.clear(); 
	}

	/**
	 * Sets a limited depth scope for the symbolic execution 
	 * of the concretization methods. 
	 * The depth of a state is the number of branches above it. If 
	 * a state has a depth greater than the depth scope the exploration 
	 * of the branch it belongs is interrupted.
	 * 
	 * @param depthScope an {@code int}, the depth scope.
	 */
	public void setConcretizationDepthScope(int depthScope) { 
		this.concretizationDepthScope = depthScope; 
	}

	/**
	 * Sets an unlimited depth scope for the symbolic execution 
	 * of the concretization methods; this is the default behaviour. 
	 * The depth of a state is the number of branches above it. If 
	 * a state has a depth greater than the depth scope the exploration 
	 * of the branch it belongs is interrupted.
	 */
	public void setConcretizationDepthScopeUnlimited() { 
		this.concretizationDepthScope = 0; 
	}

	/**
	 * Sets a limited count scope for the symbolic execution 
	 * of the concretization methods. 
	 * If a state has a number of predecessor states greater than the 
	 * count scope the exploration of the branch it belongs is interrupted.
	 * 
	 * @param countScope an {@code int}, the count scope.
	 */
	public void setConcretizationCountScope(int countScope) { 
		this.concretizationCountScope = countScope; 
	}

	/**
	 * Sets an unlimited count scope for the symbolic execution 
	 * of the concretization methods; this is the default behaviour.
	 * If a state has a number of predecessor states greater than the 
	 * count scope the exploration of the branch it belongs is interrupted.
	 */
	public void setConcretizationCountScopeUnlimited() { 
		this.concretizationCountScope = 0; 
	}
    
    /**
     * Sets whether the engine should use LICS rules
     * to decide on references resolution. By default
     * LICS rules are used.
     * 
     * @param useLICS {@code true} iff the engine must 
     * use LICS rules.
     */
    public void setUseLICS(boolean useLICS) {
        this.useLICS = useLICS;
    }
    
    /**
     * Gets whether the engine should use LICS rules
     * to decide on references resolution.
     * 
     * @return {@code true} iff the engine must 
     * use LICS rules.
     */
    public boolean getUseLICS() {
        return this.useLICS;
    }
    
    /**
     * Returns the {@link LICSRulesRepo} 
     * containing all the LICS rules that
     * must be used.
     * 
     * @return a {@link LICSRulesRepo}. It
     *         is the one that backs this
     *         {@link RunParameters}, not a
     *         safety copy.
     */
    public LICSRulesRepo getLICSRulesRepo() {
        return this.repoLICS;
    }

    /**
     * Returns the {@link ClassInitRulesRepo} 
     * containing all the class initialization 
     * rules (list of classes that are assumed
     * not to be initialized) that must be used.
     * 
     * @return a {@link ClassInitRulesRepo}. It
     *         is the one that backs this
     *         {@link EngineParameters}, not a
     *         safety copy.
     */
    public ClassInitRulesRepo getClassInitRulesRepo() {
        return this.repoInit;
    }
    
    /**
     * Specifies a LICS rule for symbolic reference expansion. By default a 
     * symbolic reference is expanded to a fresh symbolic object with class
     * of its static type, or is not expanded if the static type of the reference
     * is an abstract class or an interface.
     * This method allows to override this default.
     * 
     * @param toExpand     the static type of the reference to be expanded. 
     *                     It must be {@code toExpand != null}.
     * @param originExp    a path expression describing the origin of the 
     *                     symbolic references that match this rule.
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toExpand} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     expansions for {@code toExpand}. During  
     *                     symbolic execution, every symbolic reference with 
     *                     static type {@code toExpand} and origin matching 
     *                     {@code originExp}, will be expanded 
     *                     when necessary to a symbolic object with class 
     *                     {@code classAllowed}. If {@code classAllowed == null}, 
     *                     the matching {@link ReferenceSymbolic}s will not be expanded.
     */
	public void addExpandToLICS(String toExpand, String originExp, String classAllowed) {
		this.repoLICS.addExpandTo(toExpand, originExp, classAllowed);
	}

    /**
     * Specifies a LICS rule for symbolic reference resolution by alias. 
     * By default, symbolic references are resolved by aliases to all the 
     * type-compatible objects assumed by previous epoch-compatible expansions. 
     * This method allows to override this default.
     * 
     * @param toResolve      the static type of the reference to be resolved. 
     *                       It must be {@code toResolve != null}.
     * @param originExp      a path expression describing the origin of the 
     *                       symbolic references that match this rule.
     *                       The path expression is a slash-separated list of field
     *                       names that starts from {ROOT}:x, indicating the 
     *                       parameter with name {@code x} of the root method 
     *                       invocation (including {@code this}).
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathAllowedExp a path expression describing the objects that are 
     *                       acceptable as alias for {@code toResolve}. 
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
     *                       invocation. Start the expression with {MAX} to indicate
     *                       a max-rule.
     *                       During symbolic execution, every symbolic reference 
     *                       with class {@code toResolve} and origin matching 
     *                       {@code originExp} will be resolved when necessary 
     *                       to all the type- and epoch-compatible 
     *                       symbolic objects whose origins match
     *                       {@code pathAllowedExp}. If {@code pathAllowedExp == null}
     *                       the matching {@link ReferenceSymbolic} will not be
     *                       resolved by alias.
     */
	public void addResolveAliasOriginLICS(String toResolve, String originExp, String pathAllowedExp) {
	    this.repoLICS.addResolveAliasOrigin(toResolve, originExp, pathAllowedExp);
	}

    /**
     * Specifies a LICS rule for symbolic reference resolution by alias. 
     * By default, symbolic references are resolved by aliases to all the 
     * type-compatible objects assumed by previous epoch-compatible expansions. 
     * This method allows to override this default.
     * 
     * @param toResolve    the static type of the reference to be resolved. 
     *                     It must be {@code toResolve != null}.
     * @param originExp    a path expression describing the origin of the 
     *                     symbolic references that match this rule.
     *                     The path expression is a slash-separated list of field
     *                     names that starts from {ROOT}:x, indicating the 
     *                     parameter with name {@code x} of the root method 
     *                     invocation (including {@code this}).
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toResolve} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     aliases for {@code toResolve}. During  
     *                     symbolic execution, every symbolic reference with 
     *                     static type {@code toResolve} and origin matching 
     *                     {@code originExp}, will be resolved 
     *                     when necessary to all the epoch-compatible symbolic 
     *                     objects with class {@code classAllowed}. If 
     *                     {@code classAllowed == null} the matching 
     *                     {@link ReferenceSymbolic} will not be resolved by alias.
     */
	public void addResolveAliasInstanceofLICS(String toResolve, String originExp, String classAllowed) {
	    this.repoLICS.addResolveAliasInstanceof(toResolve, originExp, classAllowed);
	}

    /**
     * Specifies a LICS rule for symbolic reference resolution by alias. 
     * By default, symbolic references are resolved by aliases to all the 
     * type-compatible objects assumed by previous epoch-compatible expansions. 
     * This method allows to override this default.
     * 
     * @param toResolve      the static type of the reference to be resolved. 
     *                       It must be {@code toResolve != null}.
     * @param originExp      a path expression describing the origin of the 
     *                       symbolic references that match this rule.
     *                       The path expression is a slash-separated list of field
     *                       names that starts from {ROOT}:x, indicating the 
     *                       parameter with name {@code x} of the root method 
     *                       invocation (including {@code this}).
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathDisallowedExp a path expression describing the objects that are not
     *                          acceptable as alias for {@code toResolve}. 
     *                          The path expression is a slash-separated list of field
     *                          names that starts from {ROOT}:x, indicating the 
     *                          parameter with name {@code x} of the root method 
     *                          invocation (including {@code this}), or from 
     *                          {REF}, indicating a path starting from the origin 
     *                          of the reference matched by the left part of the rule. 
     *                          You can also use the special {UP} to move back in the 
     *                          path; for instance, if the reference matching 
     *                          {@code originExp} has origin 
     *                          {ROOT}:this/list/head/next/next, then you can use both 
     *                          {REF}/{UP}/{UP}/{UP} and {ROOT}:this/list to denote 
     *                          the field with name {@code list} of the object that is
     *                          referred by the {@code this} parameter of the root method
     *                          invocation.
     *                          During symbolic execution, every symbolic reference 
     *                          with class {@code toResolve} and origin matching 
     *                          {@code originExp} will not be resolved when necessary 
     *                          to a type- and epoch-compatible symbolic object 
     *                          if its origin matches {@code pathDisallowedExp}.
     */
    public void addResolveAliasNeverLICS(String toResolve, String originExp, String pathDisallowedExp) {
        this.repoLICS.addResolveAliasNever(toResolve, originExp, pathDisallowedExp);
    }

    /**
     * Specifies a LICS rule for symbolic reference resolution by null. By 
     * default all symbolic references are resolved by null. This method
     * allows to override this default.
     * 
     * @param toResolve the static type of the reference to be resolved. 
     *                  It must be {@code toResolve != null}.
     * @param originExp a path expression describing the origin of the 
     *                  symbolic references which match this rule.
     *                  The path expression is a slash-separated list of field
     *                  names that starts from {ROOT}:x, indicating the 
     *                  parameter with name {@code x} of the root method 
     *                  invocation (including {@code this}).
     *                  If {@code originExp == null}, all the symbolic 
     *                  references with static type {@code toResolve} 
     *                  will match.
     */ 
	public void addResolveNotNullLICS(String toResolve, String originExp) {
	    this.repoLICS.addResolveNotNull(toResolve, originExp);
	}

    /**
     * Adds class names to the set of not initialized classes.
     * 
     * @param notInitializedClasses a list of class names as a {@link String} varargs.
     */
    public void addNotInitializedClasses(String... notInitializedClasses) {
        this.repoInit.addNotInitializedClass(notInitializedClasses);
    }
    
    /**
     * Adds a trigger method that fires when some references are resolved by
     * expansion.
     * 
     * @param toExpand     the static type of the reference to be expanded. 
     *                     It must be {@code toExpand != null}.
     * @param originExp    a path expression describing the origin of the 
     *                     symbolic references that match this rule.
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toExpand} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     expansions for {@code toExpand}. During  
     *                     symbolic execution, every symbolic reference with 
     *                     static type {@code toExpand} and origin matching 
     *                     {@code originExp}, will be expanded 
     *                     when necessary to a symbolic object with class 
     *                     {@code classAllowed}. If {@code classAllowed == null}, 
     *                     the matching {@link ReferenceSymbolic}s will not be expanded.
     * @param triggerClassName 
     *                     the class of the instrumentation method to be triggered 
     *                     when this rule fires.
     * @param triggerParametersSignature 
     *                     the types of the parameters of the instrumentation method 
     *                     to be triggered when this rule fires.
     * @param triggerMethodName 
     *                     the name of the instrumentation method to be triggered 
     *                     when this rule fires.
     * @param triggerParameter
     *                     the parameter to be passed to the trigger when the rule fires. 
     */
    public void addExpandToTrigger(String toExpand, String originExp, String classAllowed, 
            String triggerClassName, String triggerParametersSignature, String triggerMethodName,
            String triggerParameter) {
        this.runnerParameters.addExpandToTrigger(toExpand, originExp, classAllowed, 
                triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter);
    }
    
    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toResolve      the static type of the reference to be resolved. 
     *                       It must be {@code toResolve != null}.
     * @param originExp      a path expression describing the origin of the 
     *                       symbolic references that match this rule.
     *                       The path expression is a slash-separated list of field
     *                       names that starts from {ROOT}:x, indicating the 
     *                       parameter with name {@code x} of the root method 
     *                       invocation (including {@code this}).
     *                       If {@code originExp == null}, all the symbolic 
     *                       references with static type {@code toResolve} 
     *                       will match. 
     * @param pathAllowedExp a path expression describing the objects that are 
     *                       acceptable as alias for {@code toResolve}. 
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
     *                       During symbolic execution, every symbolic reference 
     *                       with class {@code toResolve} and origin matching 
     *                       {@code originExp} will be resolved when necessary 
     *                       to all the type- and epoch-compatible 
     *                       symbolic objects whose origins match
     *                       {@code pathAllowedExp}. If {@code pathAllowedExp == null}
     *                       the matching {@link ReferenceSymbolic} will not be
     *                       resolved by alias.
     * @param triggerClassName 
     *                       the class of the instrumentation method to be triggered 
     *                       when this rule fires.
     * @param triggerParametersSignature 
     *                       the types of the parameters of the instrumentation method 
     *                       to be triggered when this rule fires.
     * @param triggerMethodName 
     *                       the name of the instrumentation method to be triggered 
     *                       when this rule fires.
     * @param triggerParameter
     *                       the parameter to be passed to the trigger when the rule fires. 
     */
    public void addResolveAliasOriginTrigger(String toResolve, String originExp, String pathAllowedExp, 
            String triggerClassName, String triggerParametersSignature, String triggerMethodName,
            String triggerParameter) {
        this.runnerParameters.addResolveAliasOriginTrigger(toResolve, originExp, pathAllowedExp, 
                triggerClassName, triggerParametersSignature, triggerMethodName, triggerParameter);
    }

    /**
     * Adds a trigger method that fires when some references are resolved by
     * alias.
     * 
     * @param toResolve    the static type of the reference to be resolved. 
     *                     It must be {@code toResolve != null}.
     * @param originExp    a path expression describing the origin of the 
     *                     symbolic references that match this rule.
     *                     The path expression is a slash-separated list of field
     *                     names that starts from {ROOT}:x, indicating the 
     *                     parameter with name {@code x} of the root method 
     *                     invocation (including {@code this}).
     *                     If {@code originExp == null}, all the symbolic 
     *                     references with static type {@code toResolve} 
     *                     will match. 
     * @param classAllowed the name of the class whose instances are possible 
     *                     aliases for {@code toResolve}. During  
     *                     symbolic execution, every symbolic reference with 
     *                     static type {@code toResolve} and origin matching 
     *                     {@code originExp}, will be resolved 
     *                     when necessary to all the epoch-compatible symbolic 
     *                     objects with class {@code classAllowed}. If 
     *                     {@code classAllowed == null} the matching 
     *                     {@link ReferenceSymbolic} will not be resolved by alias.
     * @param triggerClassName 
     *                     the class of the instrumentation method to be triggered 
     *                     when this rule fires.
     * @param triggerParametersSignature 
     *                     the types of the parameters of the instrumentation method 
     *                     to be triggered when this rule fires.
     * @param triggerMethodName 
     *                     the name of the instrumentation method to be triggered 
     *                     when this rule fires.
     * @param triggerParameter
     *                     the parameter to be passed to the trigger when the rule fires. 
     */
    public void addResolveAliasInstanceofTrigger(String toResolve, String originExp, String classAllowed, 
            String triggerClassName, String triggerParametersSignature, String triggerMethodName,
            String triggerParameter) {
        this.runnerParameters.addResolveAliasInstanceofTrigger(toResolve, originExp, classAllowed, 
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
		this.runnerParameters.addResolveNullTrigger(toResolve, originExp, triggerClassName, 
				triggerParametersSignature, triggerMethodName, triggerParameter);
	}
	
	/**
	 * Sets whether the output should be shown on 
	 * console (stdout, stderr). Note that if the 
	 * output is not shown on the console and no
	 * output file is specified (see {@link #setOutputFileName}
	 * and {@link #setOutputFileNone}) no output
	 * of the symbolic execution will be emitted.
	 * 
	 * @param showOnConsole {@code true} iff the output
	 *        should be shown on the console. By default
	 *        it is {@code true}. 
	 */
	public void setShowOnConsole(boolean showOnConsole) {
	    this.showOnConsole = showOnConsole;
	}
	
	/**
	 * Gets whether the output should be shown on 
     * console (stdout, stderr).
     * 
	 * @return a {@code boolean}.
	 */
	public boolean getShowOnConsole() {
	    return this.showOnConsole;
	}
	
	/**
	 * Sets the name of the output file.
	 * 
	 * @param s A {@link String} representing the pathname of a 
	 *          file where the console output (stdout and stderr) will 
	 *          be copied.
	 * @throws NullPointerException if {@code s == null}.
	 */
	public void setOutputFileName(String s) {
		if (s == null) {
			throw new NullPointerException();
		}
		this.outFileName = s; 
	}

	/**
	 * Instructs not to copy the console output to file, cancelling
	 * any previous invocation of the {@link #setOutputFileName}
	 * method. This is the default behaviour. 
	 */
	public void setOutputFileNone() { 
		this.outFileName = null; 
	}
	
	/**
	 * Returns the name of the output file
	 * 
	 * @return a {@link String} representing the pathname of a 
     *          file where the console output (stdout and stderr) will 
     *          be copied, or {@code null} if none was previously specified.
	 */
	public String getOutputFileName() {
	    return this.outFileName;
	}

	/**
	 * Sets the line separation text mode.
	 * 
	 * @param textMode a {@link TextMode} representing the
	 *        line separation text mode to be set.
	 * @throws NullPointerException if {@code textMode == null}.
	 */
	public void setTextMode(TextMode textMode) {
		if (textMode == null) {
			throw new NullPointerException();
		}
		this.textMode = textMode; 
	}
	
	/**
	 * Gets the line separation text mode.
	 * 
	 * @return a {@link TextMode}.
	 */
	public TextMode getTextMode() {
	    return this.textMode;
	}

	/**
	 * Sets the degree of user interaction.
	 * 
	 * @param interactionMode an {@link InteractionMode} representing the
	 *        degree of user interaction to be set.
	 * @throws NullPointerException if {@code interactionMode == null}.
	 */
	public void setInteractionMode(InteractionMode interactionMode) { 
		if (interactionMode == null) {
			throw new NullPointerException();
		}
		this.interactionMode = interactionMode; 
	}
	
	/**
	 * Gets the degree of user interaction.
	 * 
	 * @return an {@link InteractionMode}.
	 */
	public InteractionMode getInteractionMode() {
	    return this.interactionMode;
	}

	/**
	 * Sets which states will be shown on the output.
	 * 
	 * @param stepShowMode A {@link StepShowMode}.
	 * @throws NullPointerException if {@code stepShowMode == null}.
	 */
	public void setStepShowMode(StepShowMode stepShowMode) { 
		if (stepShowMode == null) {
			throw new NullPointerException();
		}
		this.stepShowMode = stepShowMode; 
	}
	
	/**
	 * Gets which states will be shown on the output.
	 * 
	 * @return a {@link StepShowMode}.
	 */
	public StepShowMode getStepShowMode() {
	    return this.stepShowMode;
	}
	
	/**
	 * Relevant only when {@link #setStepShowMode(StepShowMode)}
	 * is set to {@link StepShowMode#LEAVES} or 
	 * {@link StepShowMode#SUMMARIES} to further filter
	 * which leaves/summaries must be shown.
	 * 
	 * @param show {@code true} iff the leaves/summaries 
	 *        of safe traces must be shown.
	 */
	public void setShowSafe(boolean show) {
		if (show) {
			this.tracesToShow.add(TraceTypes.SAFE);
		} else {
			this.tracesToShow.remove(TraceTypes.SAFE);
		}
	}
	
	/**
	 * Relevant only when {@link #setStepShowMode(StepShowMode)}
	 * is set to {@link StepShowMode#LEAVES} or 
	 * {@link StepShowMode#SUMMARIES} to further filter
	 * which leaves/summaries must be shown.
	 * 
	 * @param show {@code true} iff the leaves/summaries 
	 *        of unsafe traces must be shown.
	 */
	public void setShowUnsafe(boolean show) {
		if (show) {
			this.tracesToShow.add(TraceTypes.UNSAFE);
		} else {
			this.tracesToShow.remove(TraceTypes.UNSAFE);
		}
	}
	
	/**
	 * Relevant only when {@link #setStepShowMode(StepShowMode)}
	 * is set to {@link StepShowMode#LEAVES} or 
	 * {@link StepShowMode#SUMMARIES} to further filter
	 * which leaves/summaries must be shown.
	 * 
	 * @param show {@code true} iff the leaves/summaries 
	 *        of contradictory traces must be shown.
	 */
	public void setShowContradictory(boolean show) {
		if (show) {
			this.tracesToShow.add(TraceTypes.CONTRADICTORY);
		} else {
			this.tracesToShow.remove(TraceTypes.CONTRADICTORY);
		}
	}
	
	/**
	 * Relevant only when {@link #setStepShowMode(StepShowMode)}
	 * is set to {@link StepShowMode#LEAVES} or 
	 * {@link StepShowMode#SUMMARIES} to further filter
	 * which leaves/summaries must be shown.
	 * 
	 * @param show {@code true} iff the leaves/summaries 
	 *        of out of scope traces must be shown.
	 */
	public void setShowOutOfScope(boolean show) {
		if (show) {
			this.tracesToShow.add(TraceTypes.OUT_OF_SCOPE);
		} else {
			this.tracesToShow.remove(TraceTypes.OUT_OF_SCOPE);
		}
	}
	
	/**
	 * Returns the traces types to be shown.
	 * 
	 * @return an {@link EnumSet}{@code <}{@link TraceTypes}{@code >}
	 *         containing the trace types to be shown.
	 */
	public EnumSet<TraceTypes> getTracesToShow() {
	    return this.tracesToShow.clone();
	}
	
	/**
	 * Sets the maximum stack depth to display.
	 * 
	 * @param stackDepthShow an {@code int}, the maximum depth
	 * @throws NullPointerException if {@code stackDepthShow <= 0}.
	 */
	public void setStackDepthShow(int stackDepthShow) {
		if (stackDepthShow <= 0) {
			throw new NullPointerException();
		}
		this.stackDepthShow = stackDepthShow;
	}
	
	/**
	 * Resets the maximum stack depth to display 
	 * to its default (i.e., display the whole stack).
	 */
	public void setStackDepthShowAll() {
		this.stackDepthShow = 0;
	}
	
	/**
	 * Gets the maximum stack depth to display.
	 * 
	 * @return an {@code int}, the maximum depth or {@code 0} 
	 *         if should display the whole stack.
	 */
	public int getStackDepthShow() {
	    return this.stackDepthShow;
	}
	
	/**
	 * Sets whether, at the end of each trace, it should be
	 * checked if the final state can be concretized. By 
	 * default concretization check is not performed.
	 * 
	 * @param doConcretization {@code true} iff the concretization 
	 *        check must be performed.
	 */
	public void setDoConcretization(boolean doConcretization) {
		this.doConcretization = doConcretization;		
	}
	
	/**
	 * Gets whether, at the end of each trace, it should be
     * checked if the final state can be concretized.
     * 
	 * @return a {@code boolean}.
	 */
	public boolean getDoConcretization() {
	    return this.doConcretization;
	}

    /**
     * Specifies the concretization method of a class.
     * 
     * @param className the name of a class.
     * @param methodName the name of the concretization method 
     *        contained in the class. It must be a parameterless
     *        nonnative instance method returning a boolean and it 
     *        must be defined in the class (i.e., it may not be
     *        inherited).
     */
    public void addConcretizationMethod(String className, String methodName) {
        this.concretizationMethods.put(className, methodName);
    }
    
    /**
     * Returns the concretization methods of classes.
     * 
     * @return a {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >},
     *         associating a class name to the name of its concretization method.
     */
    public Map<String, String> getConcretizationMethods() {
        return new HashMap<>(this.concretizationMethods);
    }

	/**
	 * Sets the state output format mode. 
	 * 
	 * @param stateFormatMode A {@link StateFormatMode} 
	 *        representing the output format mode of the
	 *        states.
	 * @throws NullPointerException if {@code stateFormatMode == null}.
	 */
	public void setStateFormatMode(StateFormatMode stateFormatMode) { 
		if (stateFormatMode == null) {
			throw new NullPointerException();
		}
		this.stateFormatMode = stateFormatMode; 
	}
	
	/**
	 * Gets the state output format mode.
	 * 
	 * @return A {@link StateFormatMode}.
	 */
	public StateFormatMode getStateFormatMode() {
	    return this.stateFormatMode;
	}
	
	/**
	 * Sets the path of the source files.
	 * 
	 * @param srcPath a varargs of {@link String}, the 
	 *        paths to be added to the list of source paths.
	 * @throws NullPointerException if {@code srcPath == null}.
	 */
	public void addSourcePath(String... srcPath) { 
		if (srcPath == null) {
			throw new NullPointerException();
		}
		Collections.addAll(this.srcPath, srcPath); 
	}
	
	/**
	 * Clears the paths of the source files.
	 */
	public void clearSourcePath() {
	    this.srcPath.clear();
	}
	
	/**
	 * Gets the paths of the source files.
	 * 
	 * @return a {@link List}{@code <}{@link String}{@code >}
	 */
	public List<String> getSourcePath() {
	    return new ArrayList<>(this.srcPath);
	}

    /**
     * Instructs whether the tool info produced at startup
     * (welcome message and progress of tool initialization) 
     * and at the end of symbolic execution (stats) should 
     * be logged (by default they are).
     * 
     * @param show {@code true} iff the info must
     *        be logged.
     */
    public void setShowInfo(boolean show) {
        this.showInfo = show; 
    }
    
    /**
     * Returns whether the tool info produced at startup
     * (welcome message and progress of tool initialization) 
     * and at the end of symbolic execution (stats) should 
     * be logged.
     * 
     * @return a {@code boolean}.
     */
    public boolean getShowInfo() {
        return this.showInfo;
    }

	/**
	 * Instructs whether the warnings issued during symbolic 
	 * execution should be logged (by default they are).
	 * @param show {@code true} iff the warnings must
	 *        be logged.
	 */
	public void setShowWarnings(boolean show) {
		this.showWarnings = show; 
	}

	/**
	 * Returns whether the warnings issued during symbolic 
     * execution should be logged.
	 * 
	 * @return a {@code boolean}.
	 */
    public boolean getShowWarnings() {
        return this.showWarnings;
    }

	/**
	 * Instructs whether the interactions between the runner 
	 * and the decision procedure should be logged 
	 * (by default they are not).
	 * @param show {@code true} iff the interactions must
	 *        be logged.
	 */
	public void setShowDecisionProcedureInteraction(boolean show) { 
		this.showDecisionProcedureInteraction = show; 
	}
	
	/**
	 * Returns whether the interactions between the runner 
     * and the decision procedure should be logged.
	 * 
	 * @return a {@code boolean}.
	 */
	public boolean getShowDecisionProcedureInteraction() {
	    return this.showDecisionProcedureInteraction;
	}

	/**
	 * Sets the symbolic execution to be guided by a concrete one starting
	 * from a driver method. The driver method <em>must</em> set 
	 * up all the necessary concrete inputs and then invoke the method set 
	 * by {@link #setMethodSignature}.
	 * 
	 * @param driverClass a {@link String}, the class name of the driver method. 
	 * @param driverName a {@link String}, the name of the driver method. The
	 *        method must be parameterless and have {@code void} return type.
	 * @throws NullPointerException when any parameter is {@code null}.
	 */
	public void setGuided(String driverClass, String driverName) {
		if (driverClass == null || driverName == null) {
			throw new NullPointerException();
		}
		this.guided = true;
		this.driverSignature = new Signature(driverClass, "()V", driverName); 
	}
	
	/**
	 * Sets the type of the guidance decision procedure
	 * 
	 * @param guidanceType a {@link GuidanceType}.
         * @throws NullPointerException if {@code guidanceType == null}.
	 */
	public void setGuidanceType(GuidanceType guidanceType) {
	    if (guidanceType == null) {
	        throw new NullPointerException();
	    }
	    this.guidanceType = guidanceType;
	}
	
	/**
	 * Sets ordinary symbolic execution, not guided by a concrete one.
	 * This is the default behaviour.
	 */
	public void setUnguided() {
		this.guided = false;
		this.driverSignature = null;
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
	 * Returns the decision procedure guidance type.
	 * 
	 * @return a {@link GuidanceType}.
	 */
	public GuidanceType getGuidanceType() {
	    return this.guidanceType;
	}

	/**
	 * Returns a new {@link RunnerParameters} that can be used
	 * to run a conservative repOk method.
	 * 
	 * @return a new instance of {@link RunnerParameters}.
	 */
	public RunnerParameters getConservativeRepOkDriverParameters(DecisionProcedureAlgorithms dec) {
		final RunnerParameters retVal = this.runnerParameters.clone();
		retVal.setDecisionProcedure(dec);
		retVal.setStateIdentificationMode(StateIdentificationMode.COMPACT);
		retVal.setBreadthMode(BreadthMode.MORE_THAN_ONE);
		/* TODO should be:
		 * retVal.setHeapScopeUnlimited();
		 * retVal.setDepthScopeUnlimited();
		 * retVal.setCountScopeUnlimited();
		 */
		retVal.setHeapScopeComputed(this.concretizationHeapScope);
		retVal.setDepthScope(this.concretizationDepthScope);
		retVal.setCountScope(this.concretizationCountScope);
		retVal.setIdentifierSubregionRoot();
		return retVal;
	}
	
	//TODO move these two methods, and do not use cloning but set all the parameters in a predictable way.

	/**
	 * Returns a new {@link RunnerParameters} that can be used
	 * to run a concretization method (sets only scopes).
	 * 
	 * @return a new instance of {@link RunnerParameters}.
	 */
	public RunnerParameters getConcretizationDriverParameters() {
		final RunnerParameters retVal = this.runnerParameters.clone();
		retVal.setStateIdentificationMode(StateIdentificationMode.COMPACT);
		retVal.setBreadthMode(BreadthMode.MORE_THAN_ONE);
		retVal.setHeapScopeComputed(this.concretizationHeapScope);
		retVal.setDepthScope(this.concretizationDepthScope);
		retVal.setCountScope(this.concretizationCountScope);
		retVal.setIdentifierSubregionRoot();
		return retVal;
	}

	/**
	 * Returns a new {@link RunnerParameters} that can be used
	 * to run the guidance driver method.
	 * 
	 * @param calc the {@link CalculatorRewriting} to be used by the decision procedure.
	 * @return a new instance of {@link RunnerParameters}, 
	 * or {@code null} iff {@link #isGuided()} {@code == false}.
	 */
	public RunnerParameters getGuidanceDriverParameters(CalculatorRewriting calc) {
		final RunnerParameters retVal;
		if (isGuided()) {
			retVal = this.runnerParameters.clone();
			retVal.setMethodSignature(this.driverSignature.getClassName(), this.driverSignature.getDescriptor(), this.driverSignature.getName());
			retVal.setCalculator(calc);
			retVal.setDecisionProcedure(new DecisionProcedureAlgorithms(new DecisionProcedureClassInit(new DecisionProcedureAlwSat(), calc, new ClassInitRulesRepo()), calc)); //for concrete execution
			retVal.setStateIdentificationMode(StateIdentificationMode.COMPACT);
			retVal.setBreadthMode(BreadthMode.MORE_THAN_ONE);
			retVal.setIdentifierSubregionRoot();
		} else {
			retVal = null;
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@Override 
	public RunParameters clone() {
		final RunParameters o;
		try {
			o = (RunParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e); //will not happen
		}
		o.runnerParameters = this.runnerParameters.clone();
		o.rewriterClasses = (ArrayList<Class<? extends Rewriter>>) this.rewriterClasses.clone();
		o.repoLICS = this.repoLICS.clone();
        o.repoInit = this.repoInit.clone();
		o.conservativeRepOks = (HashMap<String, String>) this.conservativeRepOks.clone();
		o.concretizationHeapScope = (HashMap<String, Function<State, Integer>>) this.concretizationHeapScope.clone();
		o.creationStrategies = (ArrayList<DecisionProcedureCreationStrategy>) this.creationStrategies.clone();
		o.tracesToShow = this.tracesToShow.clone();
		o.concretizationMethods = (HashMap<String, String>) this.concretizationMethods.clone();
		o.srcPath = (ArrayList<String>) this.srcPath.clone();
		return o;
	}
}
