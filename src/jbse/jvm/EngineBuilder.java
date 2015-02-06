package jbse.jvm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jbse.algo.ExecutionContext;
import jbse.algo.NativeInvokerPure;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.Signature;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.DecisionAlternativeComparators;

/**
 * A Builder for {@link Engine}.
 * 
 * @author Pietro Braione
 */
public class EngineBuilder {
	/** 
	 * Constructor.
	 */
	public EngineBuilder() { }
	
	/**
	 * Builds and configures an {@link Engine}.
	 *  
	 * @param parameters the {@link EngineParameters} to configure the 
	 *        {@link Engine}.
	 * @return an {@link Engine}.
	 * @throws CannotBuildEngineException whenever {@code parameters} has
	 *         insufficient information for creating an {@link Engine}.
	 * @throws DecisionException in case initialization of the 
	 *         decision procedure fails for some reason.
	 * @throws InitializationException in case the specified root method 
	 *         does not exist or cannot be symbolically executed for 
	 *         any reason (e.g., is native).
	 * @throws InvalidClassFileFactoryClassException in case the class object 
	 *         provided to build a class file factory cannot be used
	 *         (e.g., it has not a suitable constructor or it is not visible).
	 * @throws NonexistingObservedVariablesException in case some of the provided 
	 *         observed variable names cannot be observed. This is the only exception
	 *         that allows nevertheless to perform symbolic execution, in which case 
	 *         only the observers to existing variables will be notified.
	 * @throws ClasspathException in case some essential standard JRE class is missing
	 *         from the classpath or is incompatible with the current JBSE version.
	 */
	public Engine build(EngineParameters parameters) 
	throws CannotBuildEngineException, DecisionException, InitializationException, 
	InvalidClassFileFactoryClassException, NonexistingObservedVariablesException, 
	ClasspathException {
		//checks whether parameters is complete
		if (parameters.getMethodSignature() == null && parameters.getInitialState() == null) {
			throw new CannotBuildEngineException(new NullPointerException());
		}

		//creates the engine
		final Engine engine = bootEngineArchitecture(parameters);
		engine.init();
		return engine;
	}
	
	private static Engine bootEngineArchitecture(EngineParameters parameters) 
	throws CannotBuildEngineException {
		final ExecutionContext ctx = new ExecutionContext(
				parameters.getInitialState(),
				parameters.getClasspath(),
				parameters.getMethodSignature(),
				parameters.getCalculator(),
				parameters.getDecisionProcedure(),
				parameters.getStateIdentificationMode().toInternal(), 
				parameters.getBreadthMode().toInternal(),
				ClassFileFactoryJavassist.class,      //default
				getExpansionBackdoor(parameters), 
				getRulesTrigger(parameters), 
				new DecisionAlternativeComparators(), //default 
				new NativeInvokerPure()               //default
				);
		
		//sets the meta-level directives
		setMeta(ctx, parameters);
		
		final VariableObserverManager vom = new VariableObserverManager(parameters.getMethodSignature().getClassName());

        //sets the observers
        setObservers(vom, parameters);

		return new Engine(ctx, vom);
	}
	
	private static Map<String, Set<String>> getExpansionBackdoor(EngineParameters parameters) {
		final HashMap<String, Set<String>> retVal = new HashMap<>();
		for (String[] rule : parameters.expandToTriggers) {
			final String toExpand = rule[0];
			final String classAllowed = rule[2];
			if (classAllowed == null) {
				continue;
			}
			Set<String> classesAllowed = retVal.get(toExpand);
			if (classesAllowed == null) {
				classesAllowed = new HashSet<>();
				retVal.put(toExpand, classesAllowed);
			}
			classesAllowed.add(classAllowed);
		}
		return retVal;
	}

	private static TriggerRulesRepo getRulesTrigger(EngineParameters parameters) {
		final TriggerRulesRepo retVal = new TriggerRulesRepo();
		
		for (String[] rule : parameters.expandToTriggers) {
			if (rule[3] == null) { continue; }
			retVal.addExpandTo(rule[0], rule[1], rule[2], 
					new Signature(rule[3], rule[4], rule[5]), rule[6]);
		}
		for (String[] rule : parameters.resolveAliasOriginTriggers) {
			if (rule[3] == null) { continue; }
			retVal.addResolveAliasOrigin(rule[0], rule[1], rule[2], 
					new Signature(rule[3], rule[4], rule[5]), rule[6]);
		}
		for (String[] rule : parameters.resolveAliasInstanceofTriggers) {
			if (rule[3] == null) { continue; }
			retVal.addResolveAliasInstanceof(rule[0], rule[1], rule[2],
					new Signature(rule[3], rule[4], rule[5]), rule[6]);
		}
		for (String[] rule : parameters.resolveNullTriggers) {
			if (rule[3] == null) { continue; }
			retVal.addResolveNull(rule[0], rule[1], 
					new Signature(rule[2], rule[3], rule[4]), rule[5]);
		}
		return retVal;
	}
	
	private static void setMeta(ExecutionContext ctx, EngineParameters parameters) {
		for (String[] rule : parameters.metaOverridden) {
			try {
				ctx.addMetaOverridden(rule[0], rule[1], rule[2], rule[3]);
			} catch (MetaUnsupportedException e) {
				// TODO manage the situation
			}
		}
		for (String[] rule : parameters.uninterpreted) {
			ctx.addUninterpreted(rule[0], rule[1], rule[2], rule[3]);
		}
	}
	
	private static void setObservers(VariableObserverManager vom, EngineParameters parameters) {
		for (int i = 0; i < parameters.observedVars.size(); ++i) {
			vom.addObserver(parameters.observedVars.get(i), parameters.observers.get(i));
		}
	}
}
