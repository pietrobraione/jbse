package jbse.bc.exc;

/**
 * Exception thrown in all the situations (e.g., method resolution and lookup)
 * where a {@link java.lang.IncompatibleClassChangeError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class IncompatibleClassFileException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1277275222587627793L;

	public IncompatibleClassFileException(String param) {
		super(param);
    }
}
