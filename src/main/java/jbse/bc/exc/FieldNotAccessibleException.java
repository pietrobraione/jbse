package jbse.bc.exc;

public class FieldNotAccessibleException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5550985781719881944L;
	
    public FieldNotAccessibleException(String fieldSignature) {
    	super(fieldSignature);
    }
}
