package jbse.mem.exc;

/**
 *Exception for code index out of bound 
 */

public class InvalidProgramCounterException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = -4715190713854760210L;

    /**
     *Constructor
     */
    public InvalidProgramCounterException(String param)
    {
	super(param);
    }
    public InvalidProgramCounterException()
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
