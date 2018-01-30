package jbse.mem.exc;

/**
 * This exception is raised whenever a candidate assumption 
 * is unsatisfiable under the current path condition.
 *  
 * @author Pietro Braione
 */
public class ContradictionException extends Exception {
	public ContradictionException() { super(); }
	
	public ContradictionException(String s) { super(s); }

	/**
	 * 
	 */
	private static final long serialVersionUID = -3704036252506120129L;

}
