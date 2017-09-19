package jbse.bc.exc;

public class FieldNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5125092750219965116L;
	
	public FieldNotFoundException(String fieldSignature) {
		super(fieldSignature);
	}

}
