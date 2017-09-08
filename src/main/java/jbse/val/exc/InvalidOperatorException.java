package jbse.val.exc;

public class InvalidOperatorException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5542990490370684757L;
	
	public InvalidOperatorException(String operator) {
		super(operator);
	}
}
