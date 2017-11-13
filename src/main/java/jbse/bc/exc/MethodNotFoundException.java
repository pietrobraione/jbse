package jbse.bc.exc;

/**
 * Exception thrown in all the situations (e.g., method resolution and lookup)
 * where a {@link java.lang.NoSuchMethodError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class MethodNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -964367016588053873L;
	
	public MethodNotFoundException(String methodSignature) {
		super(methodSignature);
	}

}
