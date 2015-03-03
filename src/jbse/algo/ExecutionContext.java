package jbse.algo;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.algo.meta.DispatcherMeta;
import jbse.bc.ClassFileFactory;
import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.State;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.DecisionAlternativeComparators;
import jbse.tree.StateTree;
import jbse.tree.StateTree.BreadthMode;
import jbse.tree.StateTree.StateIdentificationMode;
import jbse.val.Calculator;

/**
 * Class containing an execution context, i.e., everything 
 * different from the symbolic execution state necessary to 
 * perform an execution step.
 * 
 * @author Pietro Braione
 */
public final class ExecutionContext {
    /** The {@link Classpath}. Used during initialization. */
    public final Classpath classpath;
    
	/** The {@link Signature} of the root (initial) method. Used during initialization. */
	public final Signature rootMethodSignature;
	
	/** The {@link Calculator}. Used during initialization. */
	public final Calculator calc;
    
    /** The class for the symbolic execution's {@link ClassFileFactory} 
     * (injected dependency). Used during initialization.
     */
    public final Class<? extends ClassFileFactory> classFileFactoryClass;

    /** 
     * Maps class names to the names of the subclasses that may be 
     * used to expand references. Used during initialization.
     */
    public final Map<String, Set<String>> expansionBackdoor;
    
    /** 
     * The initial {@link State} of symbolic execution. It is a prototype 
     * that will be cloned by its getter. 
     */
    private State initialState;

    /** The symbolic execution's {@link DecisionAlternativeComparators}. */
    private final DecisionAlternativeComparators comparators;
    
    /** 
     * The {@link DispatcherMeta} for handling methods with 
     * meta-level implementation. 
     */
    public final DispatcherMeta dispatcherMeta = new DispatcherMeta();
	
	/** The symbolic execution's {@link DecisionProcedureAlgorithms}. */
	public final DecisionProcedureAlgorithms decisionProcedure;
	
	/** The symbolic execution's {@link StateTree}. */
	public final StateTree stateTree;
	
	/** The symbolic execution's {@link NativeInvoker}. */
	public final NativeInvoker nativeInvoker;
	
	/** 
	 * The {@link TriggerManager} that handles reference resolution events
	 * and executes triggers. 
	 */
	public final TriggerManager triggerManager;
	
	/**
	 * Constructor.
	 * 
	 * @param the initial {@code State}, or {@code null} if no
	 *        initial state. Warning: all the remaining parameters
	 *        must be coherent with it, if not {@code null} (e.g., 
	 *        {@code calc} must be the calculator used to create  
	 *        {@code initialState}). It will not be modified, but
	 *        it shall not be modified externally.
	 * @param classpath a {@link Classpath} object, containing 
	 *        information about the classpath of the symbolic execution.
	 * @param rootMethodSignature the {@link Signature} of the root method
	 *        of the symbolic execution.
	 * @param calc a {@link Calculator}.
	 * @param decisionProcedure a {@link DecisionProcedureAlgorithms}.
	 * @param stateIdentificationMode a {@link StateIdentificationMode}.
	 * @param breadthMode a {@link BreadthMode}.
	 * @param classFileFactoryClass a {@link Class}{@code <? extends }{@link ClassFileFactory}{@code >}
	 *        that will be instantiated by the engine to retrieve classfiles. It must 
	 *        provide a parameterless public constructor.
	 * @param expansionBackdoor a 
	 *        {@link Map}{@code <}{@link String}{@code , }{@link Set}{@code <}{@link String}{@code >>}
	 *        associating class names to sets of names of their subclasses. It 
	 *        is used in place of the class hierarchy to perform reference expansion.
	 * @param rulesTrigger a {@link TriggerRulesRepo}.
	 * @param comparators a {@link DecisionAlternativeComparators} which
	 *        will be used to establish the order of exploration
	 *        for sibling branches.
	 * @param nativeInvoker a {@link NativeInvoker} which will be used
	 *        to execute native methods.
	 */
    public ExecutionContext(
    		State initialState,
    		Classpath classpath,
    		Signature rootMethodSignature,
    		Calculator calc, 
    		DecisionProcedureAlgorithms decisionProcedure,
    		StateIdentificationMode stateIdentificationMode,
    		BreadthMode breadthMode,
    		Class<? extends ClassFileFactory> classFileFactoryClass, 
    		Map<String, Set<String>> expansionBackdoor,
    		TriggerRulesRepo rulesTrigger,
    		DecisionAlternativeComparators comparators, 
    		NativeInvoker nativeInvoker) {
    	this.initialState = initialState;
    	this.classpath = classpath;
		this.rootMethodSignature = rootMethodSignature;
    	this.calc = calc;
		this.decisionProcedure = decisionProcedure;
		this.stateTree = new StateTree(stateIdentificationMode, breadthMode);
		this.classFileFactoryClass = classFileFactoryClass;
		this.expansionBackdoor = expansionBackdoor;
		this.triggerManager = new TriggerManager(rulesTrigger);
		this.comparators = comparators;
		this.nativeInvoker = nativeInvoker;
    }
    
    /**
     * Sets the initial state. To be invoked whenever 
     * the engine parameters object provided through the 
     * constructor does not have an initial state.
     * 
     * @param initialState a {@link State}. The method
     *        stores in this execution contest a safety 
     *        copy of it.
     */
    public void setInitialState(State initialState) {
    	this.initialState = initialState.clone();
    }
    
    /**
     * Returns the initial state.
     * 
     * @return a {@link State}, a clone of the initial state
     *         of the symbolic execution.
     */
    public State getInitialState() {
    	return (this.initialState == null ? null : this.initialState.clone());
    }    
	
	/**
	 * Allows to customize the behavior of the invocations to a method 
	 * by specifying an {@link Algorithm} that implements its semantics.
	 * 
	 * @param className a class name as a {@link String}.
	 * @param paramsSig the signature of the method's parameters as a 
	 *                  {@link String}.
	 * @param methodName the name of the method as a {@link String}. 
	 * @param metaDelegateClassName a class name as a {@link String}, 
	 *        indicating a class (that must be in the meta-level classpath, 
	 *        must have a default constructor, must implement {@link Algorithm})
	 *        of an algorithm that implements at the meta-level the 
	 *        semantics of the invocations to {@code className.methodName}. 
	 * @throws MetaUnsupportedException if the class indicated in 
	 *         {@code metaDelegateClassName} does not exist, or cannot be loaded 
	 *         or instantiated for any reason (misses from the meta-level classpath, 
	 *         has insufficient visibility, does not implement {@link Algorithm}...).
	 */
	public void addMetaOverridden(String className, String paramsSig, String methodName, String metaDelegateClassName) 
	throws MetaUnsupportedException {
	    final Signature sig = new Signature(className, paramsSig, methodName);
	    try {
	        final Class<? extends Algorithm> metaDelegateClass = ClassLoader.getSystemClassLoader().loadClass(metaDelegateClassName).asSubclass(Algorithm.class);
	        this.dispatcherMeta.loadAlgoMetaOverridden(sig, metaDelegateClass);
	    } catch (ClassNotFoundException e) {
	        throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClassName + " not found.");
	    } catch (ClassCastException e) {
	        throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClassName + " does not implement " + Algorithm.class);
	    }
	}
	
	/**
	 * Allows to customize the behavior of the invocations to a method 
	 * by treating all the invocations of a given method as returning 
	 * the application of an uninterpreted symbolic function
	 * with no side effect.
	 * 
	 * @param className a class name as a {@link String}.
	 * @param paramsSig the signature of the method's parameters as a 
	 *                  {@link String}.
	 * @param methodName the name of the method as a {@link String}. 
	 * @param functionName the name of the uninterpreted symbolic function
	 *        whose application to the invocation parameter is 
	 *        the result of all the invocations to {@code className.methodName}.
	 */
	public void addUninterpreted(String className, String paramsSig, String methodName, String functionName) { 
		final Signature sig = new Signature(className, paramsSig, methodName);
		this.dispatcherMeta.loadAlgoUninterpreted(sig, functionName);
	}
	
	public <R> SortedSet<R> mkDecisionResultSet(Class<R> superclassDecisionAlternatives) {
		final Comparator<R> comparator = this.comparators.get(superclassDecisionAlternatives);
		final TreeSet<R> retVal = new TreeSet<>(comparator);
		return retVal;
	}
}
