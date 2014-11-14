package jbse.bc.exc;

/**
 * Raised whenever the architecture of JBSE cannot be bootstrapped.
 * 
 * @author Pietro Braione
 *
 */
public class InvalidClassFileFactoryClassException extends Exception {
	
	public InvalidClassFileFactoryClassException() { super(); }
	
	public InvalidClassFileFactoryClassException(String s) { super(s); }

	public InvalidClassFileFactoryClassException(Throwable t) { super(t); }

	/**
	 * 
	 */
	private static final long serialVersionUID = -6554446750094089415L;

}
