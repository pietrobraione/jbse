package jbse.exc.bc;

public class ClassFileNotAccessibleException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9153894972388905388L;
	
    public ClassFileNotAccessibleException(String classSignature) {
    	super(classSignature);
    }
}
