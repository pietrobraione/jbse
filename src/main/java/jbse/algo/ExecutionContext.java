package jbse.algo;

import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASS_DESIREDASSERTIONSTATUS0;
import static jbse.bc.Signatures.JAVA_CLASS_GETCLASSLOADER0;
import static jbse.bc.Signatures.JAVA_CLASS_GETCOMPONENTTYPE;
import static jbse.bc.Signatures.JAVA_CLASS_GETPRIMITIVECLASS;
import static jbse.bc.Signatures.JAVA_CLASS_ISINSTANCE;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_HASHSET;
import static jbse.bc.Signatures.JAVA_IDENTITYHASHMAP;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE;
import static jbse.bc.Signatures.JAVA_LINKEDLIST;
import static jbse.bc.Signatures.JAVA_LINKEDLIST_ENTRY;
import static jbse.bc.Signatures.JAVA_NUMBER;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_OBJECT_GETCLASS;
import static jbse.bc.Signatures.JAVA_OBJECT_HASHCODE;
import static jbse.bc.Signatures.JAVA_REFLECT_ARRAY_NEWARRAY;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_CASEINSCOMP;
import static jbse.bc.Signatures.JAVA_STRING_HASHCODE;
import static jbse.bc.Signatures.JAVA_STRING_INTERN;
import static jbse.bc.Signatures.JAVA_SYSTEM_ARRAYCOPY;
import static jbse.bc.Signatures.JAVA_SYSTEM_IDENTITYHASHCODE;
import static jbse.bc.Signatures.JAVA_THROWABLE_FILLINSTACKTRACE;
import static jbse.bc.Signatures.JAVA_THROWABLE_GETSTACKTRACEDEPTH;
import static jbse.bc.Signatures.JAVA_THROWABLE_GETSTACKTRACEELEMENT;
import static jbse.bc.Signatures.JAVA_TREESET;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ANY;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ENDGUIDANCE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_FAIL;
import static jbse.bc.Signatures.JBSE_ANALYSIS_IGNORE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISRESOLVED;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISRUNBYJBSE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SUCCEED;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFileFactory;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.State;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.DecisionAlternative;
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

	/** The {@link DispatcherBytecodeAlgorithm}. */
	public final DispatcherBytecodeAlgorithm dispatcher = new DispatcherBytecodeAlgorithm();

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
		this.expansionBackdoor = new HashMap<>(expansionBackdoor);      //safety copy
		this.triggerManager = new TriggerManager(rulesTrigger.clone()); //safety copy
		this.comparators = comparators;
		this.nativeInvoker = nativeInvoker;
	    //defaults
        try {
            //JRE methods
            addMetaOverridden(JAVA_CLASS_DESIREDASSERTIONSTATUS0,       "jbse/algo/meta/Algo_JAVA_CLASS_DESIREDASSERTIONSTATUS0");
            addMetaOverridden(JAVA_CLASS_GETCLASSLOADER0,               "jbse/algo/meta/Algo_JAVA_CLASS_GETCLASSLOADER0");
            addMetaOverridden(JAVA_CLASS_GETCOMPONENTTYPE,              "jbse/algo/meta/Algo_JAVA_CLASS_GETCOMPONENTTYPE");
            addMetaOverridden(JAVA_CLASS_GETPRIMITIVECLASS,             "jbse/algo/meta/Algo_JAVA_CLASS_GETPRIMITIVECLASS");
            addMetaOverridden(JAVA_CLASS_ISINSTANCE,                    "jbse/algo/meta/Algo_JAVA_CLASS_ISINSTANCE");
            addMetaOverridden(JAVA_OBJECT_GETCLASS,                     "jbse/algo/meta/Algo_JAVA_OBJECT_GETCLASS");
            addMetaOverridden(JAVA_OBJECT_HASHCODE,                     "jbse/algo/meta/Algo_JAVA_OBJECT_HASHCODE");
            addMetaOverridden(JAVA_REFLECT_ARRAY_NEWARRAY,              "jbse/algo/meta/Algo_JAVA_REFLECT_ARRAY_NEWARRAY");
            addMetaOverridden(JAVA_STRING_HASHCODE,                     "jbse/algo/meta/Algo_JAVA_STRING_HASHCODE");
            addMetaOverridden(JAVA_STRING_INTERN,                       "jbse/algo/meta/Algo_JAVA_STRING_INTERN");
            addMetaOverridden(JAVA_SYSTEM_ARRAYCOPY,                    "jbse/algo/meta/Algo_JAVA_SYSTEM_ARRAYCOPY");
            addMetaOverridden(JAVA_SYSTEM_IDENTITYHASHCODE,             "jbse/algo/meta/Algo_JAVA_SYSTEM_IDENTITYHASHCODE");
            addMetaOverridden(JAVA_THROWABLE_FILLINSTACKTRACE,          "jbse/algo/meta/Algo_JAVA_THROWABLE_FILLINSTACKTRACE");
            addMetaOverridden(JAVA_THROWABLE_GETSTACKTRACEDEPTH,        "jbse/algo/meta/Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH");
            addMetaOverridden(JAVA_THROWABLE_GETSTACKTRACEELEMENT,      "jbse/algo/meta/Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT");

            //jbse.meta.Analysis methods
            addMetaOverridden(JBSE_ANALYSIS_ANY,                        "jbse/algo/meta/Algo_JBSE_ANALYSIS_ANY");
            addMetaOverridden(JBSE_ANALYSIS_ENDGUIDANCE,                "jbse/algo/meta/Algo_JBSE_ANALYSIS_ENDGUIDANCE");
            addMetaOverridden(JBSE_ANALYSIS_FAIL,                       "jbse/algo/meta/Algo_JBSE_ANALYSIS_FAIL");
            addMetaOverridden(JBSE_ANALYSIS_IGNORE,                     "jbse/algo/meta/Algo_JBSE_ANALYSIS_IGNORE");
            addMetaOverridden(JBSE_ANALYSIS_ISRESOLVED,                 "jbse/algo/meta/Algo_JBSE_ANALYSIS_ISRESOLVED");
            addMetaOverridden(JBSE_ANALYSIS_ISRUNBYJBSE,                "jbse/algo/meta/Algo_JBSE_ANALYSIS_ISRUNBYJBSE");
            addMetaOverridden(JBSE_ANALYSIS_SUCCEED,                    "jbse/algo/meta/Algo_JBSE_ANALYSIS_SUCCEED");
            addMetaOverridden(JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED,  "jbse/algo/meta/Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED");
        } catch (MetaUnsupportedException e) {
            throw new UnexpectedInternalException(e);
        }
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
	 * @param methodSignature the {@link Signature} of a method. 
	 * @param metaDelegateClassName a class name as a {@link String}, 
	 *        indicating a class (that must be in the meta-level classpath, 
	 *        must have a default constructor, must implement {@link Algorithm})
	 *        of an algorithm that implements at the meta-level the 
	 *        semantics of the invocations to the method with signature 
	 *        {@code methodSignature}. 
	 * @throws MetaUnsupportedException if the class indicated in 
	 *         {@code metaDelegateClassName} does not exist, or cannot be loaded 
	 *         or instantiated for any reason (misses from the meta-level classpath, 
	 *         has insufficient visibility, does not extend {@link Algorithm}...).
	 */
	public void addMetaOverridden(Signature methodSignature, String metaDelegateClassName) 
	throws MetaUnsupportedException {
		try {
			@SuppressWarnings("unchecked")
			final Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>> metaDelegateClass = 
				(Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>>) 
				ClassLoader.getSystemClassLoader().loadClass(metaDelegateClassName.replace('/', '.')).asSubclass(Algo_INVOKEMETA.class);
			this.dispatcherMeta.loadAlgoMetaOverridden(methodSignature, metaDelegateClass);
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
	 * @param methodSignature the {@link Signature} of a method. 
	 * @param functionName the name of the uninterpreted symbolic function
	 *        whose application to the invocation parameter is 
	 *        the result of all the invocations to {@code className.methodName}.
	 */
	public void addUninterpreted(Signature methodSignature, String functionName) { 
		this.dispatcherMeta.loadAlgoUninterpreted(methodSignature, functionName);
	}
	
    /**
     * Determines whether a class has a pure static initializer, where with
     * "pure" we mean that its effect is independent on when the initializer
     * is executed.
     * 
     * @param classHierarchy a {@link ClassHierarchy}.
     * @param className the name of the class.
     * @return {@code true} iff the class has a pure static initializer.
     */
	public boolean hasClassAPureInitializer(ClassHierarchy hier, String className) {
        return 
         	(className.equals(JAVA_CLASS)      ||
             className.equals(JAVA_HASHSET)    || className.equals(JAVA_IDENTITYHASHMAP)      || 
             className.equals(JAVA_INTEGER)    || className.equals(JAVA_INTEGER_INTEGERCACHE) || 
             className.equals(JAVA_LINKEDLIST) || className.equals(JAVA_LINKEDLIST_ENTRY)     ||
             className.equals(JAVA_NUMBER)     || className.equals(JAVA_OBJECT)               ||
             className.equals(JAVA_STRING)     || className.equals(JAVA_STRING_CASEINSCOMP)   ||
             className.equals(JAVA_BOOLEAN)    || className.equals(JAVA_TREESET)   ||
             hier.isSubclass(className, JAVA_ENUM));
	}

	public <R extends DecisionAlternative> 
	SortedSet<R> mkDecisionResultSet(Class<R> superclassDecisionAlternatives) {
		final Comparator<R> comparator = this.comparators.get(superclassDecisionAlternatives);
		final TreeSet<R> retVal = new TreeSet<>(comparator);
		return retVal;
	}
}
