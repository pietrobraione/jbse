package jbse.dec.exc;

/**
 * Exception for undefined instruction algorithm
 */
public class ExternalProtocolInterfaceException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2184263904379609801L;
	
	/**
     *Constructor
     */
    public  ExternalProtocolInterfaceException(String param) { super(param); }
    public  ExternalProtocolInterfaceException() { super(); }
    public ExternalProtocolInterfaceException(Exception e) { super(e); }
    
	/**
     *Returns the message of Exception
     */
    public String getMessage(){
	return(super.getMessage());
    }
}
