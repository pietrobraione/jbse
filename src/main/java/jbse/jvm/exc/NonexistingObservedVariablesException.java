package jbse.jvm.exc;

import java.util.List;

import jbse.jvm.Engine;


/**
 * Exception which is raised during the initialization of an {@link Engine}
 * whenever some of the provided observed variables cannot be observed.
 * 
 * @author pietro
 */
public class NonexistingObservedVariablesException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3586410176525210628L;
	private List<Integer> vars;
	
	public NonexistingObservedVariablesException(List<Integer> vars) {
		this.vars = vars;
	}
	
	/**
	 * Returns the list of the variables which cannot be observed.
	 * 
	 * @return a <code>List&lt;Integer&gt;</code> containing the indices in the  
	 *         parameter of the last call to <code>SEEngine.setObservers</code> 
	 *         which correspond to variables which will not be observed.
	 */
	public List<Integer> getVariableIndices() {
		return this.vars;
	}
}
