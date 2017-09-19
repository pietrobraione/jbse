package jbse.val.exc;

public class InvalidOperandException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4701458498926706576L;

	public InvalidOperandException(String operand) {
		super(operand);
	}
}
