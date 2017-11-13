package jbse.bc.exc;

/**
 * Exception thrown in all the situations (e.g., method resolution and lookup)
 * where a {@link java.lang.AbstractMethodError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class MethodAbstractException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4702133497596875920L;

	public MethodAbstractException(String methodSignature) {
		super(methodSignature);
	}

}
