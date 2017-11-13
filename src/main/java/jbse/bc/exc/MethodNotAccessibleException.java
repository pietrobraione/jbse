package jbse.bc.exc;

/**
 * Exception thrown in all the situations (e.g., method resolution and lookup)
 * where a {@link java.lang.IllegalAccessError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class MethodNotAccessibleException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6978511803822297340L;

    public MethodNotAccessibleException(String methodSignature) {
    	super(methodSignature);
    }
}
