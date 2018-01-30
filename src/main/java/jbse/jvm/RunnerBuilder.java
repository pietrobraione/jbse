package jbse.jvm;

import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.exc.ContradictionException;

public class RunnerBuilder {
	/**
	 * Constructor.
	 */
	public RunnerBuilder() { }
	
	/** Used to build e. */
	private final EngineBuilder eb = new EngineBuilder();

	/** The {@link Engine} underlying the built {@link Runner}. */
	private Engine engine;

	/**
	 * Builds a {@link Runner}.
	 * 
	 * @param parameters the {@link RunnerParameters} to configure the {@link Runner}.
	 * 
	 * @return a {@link Runner}.
	 * @throws CannotBuildEngineException whenever {@code parameters} has
	 *         insufficient information for creating a {@link Runner}.
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
	public Runner build(RunnerParameters parameters) 
	throws CannotBuildEngineException, DecisionException, InitializationException, 
	InvalidClassFileFactoryClassException, NonexistingObservedVariablesException, ClasspathException, 
	NotYetImplementedException, ContradictionException {
		this.engine = this.eb.build(parameters.getEngineParameters());
		return new Runner(this.engine, parameters.getActions(), parameters.getIdentifierSubregion(), 
				parameters.getTimeout(), parameters.getHeapScope(), parameters.getDepthScope(), 
				parameters.getCountScope());
	}
	
	/**
	 * Returns the {@link Engine} underlying the built {@link Runner}.
	 * 
	 * @return an {@link Engine}, or {@code null} if creation failed.
	 */
	public Engine getEngine() {
		return this.engine;
	}
}
