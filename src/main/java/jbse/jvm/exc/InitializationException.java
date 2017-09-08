package jbse.jvm.exc;

public class InitializationException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 9033044842634980880L;
    
    public InitializationException(String param) {
        super(param);
    }
    
	public InitializationException(Throwable cause) {
		super(cause);
	}
}
