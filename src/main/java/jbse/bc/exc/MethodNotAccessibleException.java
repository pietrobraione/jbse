package jbse.bc.exc;

//throw new JavaReifyException("java/lang/IllegalAccessError");

public class MethodNotAccessibleException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6978511803822297340L;

    public MethodNotAccessibleException(String methodSignature) {
    	super(methodSignature);
    }
}
