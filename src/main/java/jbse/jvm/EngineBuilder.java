package jbse.jvm;

import java.io.IOException;
import java.util.List;

import jbse.algo.ExecutionContext;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.Signature;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.exc.ContradictionException;
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
     *         from the bootstrap classpath, or is ill-formed, or cannot access one of its
     *         superclasses/superinterfaces.
     * @throws NotYetImplementedException if the trigger methods for the initial root 
     *         object expansion (when present) are not in the root class.
     * @throws ContradictionException  if some initialization assumption is
     *         contradicted.
     */
    public Engine build(EngineParameters parameters) 
    throws CannotBuildEngineException, DecisionException, InitializationException, 
    InvalidClassFileFactoryClassException, NonexistingObservedVariablesException, 
    ClasspathException, NotYetImplementedException, ContradictionException {
        //checks whether parameters is complete
        if (parameters.getMethodSignature() == null && parameters.getStartingState() == null) {
            throw new CannotBuildEngineException(new NullPointerException());
        }

        //creates the engine
        final Engine engine = bootEngineArchitecture(parameters);
        engine.init();
        return engine;
    }

    private static Engine bootEngineArchitecture(EngineParameters parameters) 
    throws CannotBuildEngineException {
    	try {
	        final ExecutionContext ctx = 
	          new ExecutionContext(parameters.getStartingState(),
	                               parameters.getBypassStandardLoading(),
	                               parameters.getMaxSimpleArrayLength(),
	                               parameters.getMaxHeapSize(),
	                               parameters.getMakePreInitClassesSymbolic(),
	                               parameters.getClasspath(),
	                               ClassFileFactoryJavassist.class,          //default
	                               parameters.getExpansionBackdoor(), 
	                               parameters.getModelClassSubstitutions(),
	                               parameters.getCalculator(),
	                               new DecisionAlternativeComparators(),     //default 
	                               parameters.getMethodSignature(),
	                               parameters.getDecisionProcedure(),
	                               parameters.getStateIdentificationMode().toInternal(), 
	                               parameters.getBreadthMode().toInternal(),
	                               parameters.getTriggerRulesRepo(),
	                               parameters.getClassInvariantAfterInitialization());
	
	        //sets the meta-level directives
	        setOverrides(ctx, parameters);
	
	        final VariableObserverManager vom = new VariableObserverManager(parameters.getMethodSignature().getClassName());
	
	        //sets the observers
	        setObservers(vom, parameters);
	
	        //creates the engine
	        final Engine retVal = new Engine(ctx, vom);
	        
	        //sets the state suppliers for the decision procedure
	        parameters.getDecisionProcedure().setInitialStateSupplier(retVal::getInitialState);
	        parameters.getDecisionProcedure().setCurrentStateSupplier(retVal::getCurrentState);
	        
	        return retVal;
    	} catch (IOException e) {
    		throw new CannotBuildEngineException(e);
    	}
    }

    private static void setOverrides(ExecutionContext ctx, EngineParameters parameters) {
        for (String[] rule : parameters.getMetaOverridden()) {
            try {
                ctx.addMetaOverridden(new Signature(rule[0], rule[1], rule[2]), rule[3]);
            } catch (MetaUnsupportedException e) {
                // TODO manage the situation
            }
        }
        for (String[] rule : parameters.getUninterpreted()) {
            ctx.addUninterpreted(new Signature(rule[0], rule[1], rule[2]));
        }
        
        for (String[] rule : parameters.getUninterpretedPattern()) {
            try {
				ctx.addUninterpretedPattern(rule[0], rule[1], rule[2]);
			} catch (InvalidInputException e) {
                // TODO manage the situation
			}
        }
    }

    private static void setObservers(VariableObserverManager vom, EngineParameters parameters) {
        final List<Signature> observedFields = parameters.getObservedFields();
        final List<ExecutionObserver> observers = parameters.getObservers();
        for (int i = 0; i < observedFields.size(); ++i) {
            vom.addObserver(observedFields.get(i), observers.get(i));
        }
    }
}
