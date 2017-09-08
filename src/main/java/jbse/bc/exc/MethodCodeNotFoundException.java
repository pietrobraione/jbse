package jbse.bc.exc;

public class MethodCodeNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8794118883884362801L;
	
	public MethodCodeNotFoundException(String methodSignature) {
		super(methodSignature);
	}

}
