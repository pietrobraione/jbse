package jbse.bc.exc;

public class MethodAbstractException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4702133497596875920L;

	public MethodAbstractException(String methodSignature) {
		super(methodSignature);
	}

}
