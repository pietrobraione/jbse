package jbse.algo.exc;
/**
 *Exception for undefined Operator
 */

public class InvalidOperatorException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1468751036274766143L;

	/**
     *Constructor
     */
    public InvalidOperatorException(String param)
    {
	super(param);
    }
    /**
     *Constructor by default
     */
    public InvalidOperatorException()
    {
	super();
    }
    /**
     *Returns the message of Exception
     */
    public String getMessage()
    {
	return(super.getMessage());
    }
}
