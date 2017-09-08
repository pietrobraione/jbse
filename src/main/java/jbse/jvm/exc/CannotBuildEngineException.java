package jbse.jvm.exc;

import jbse.jvm.Engine;
import jbse.jvm.EngineBuilder;
import jbse.jvm.EngineParameters;

/**
 * Exception thrown by {@link EngineBuilder} when it cannot build
 * an {@link Engine} because of insufficient {@link EngineParameters}.
 * 
 * @author Pietro Braione
 *
 */
public class CannotBuildEngineException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7777524301395514296L;
	
	public CannotBuildEngineException(String s) { super(s); }
	
	public CannotBuildEngineException(Exception e) { super(e); }

    public CannotBuildEngineException() { super(); }

}
