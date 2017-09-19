package jbse.val.exc;

/**
 * Exception thrown whenever there is a type error: a string does not describe
 * an appropriate type name, an operator is applied to the operands with wrong 
 * type, etc. 
 * 
 * @author Pietro Braione
 */
public class InvalidTypeException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3857254096423714994L;

    public InvalidTypeException(String param) {
    	super(param);
    }
}
