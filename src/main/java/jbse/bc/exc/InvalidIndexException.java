package jbse.bc.exc;

/**
 * Exception thrown whenever an indexed access to a class 
 * constant pool was not successful.
 * 
 * @author Pietro Braione
 *
 */
public class InvalidIndexException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3556048401417493206L;
	
	public InvalidIndexException(String s) {
		super(s);
	}

}
