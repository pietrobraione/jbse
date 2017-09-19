package jbse.dec.exc;

/**
 * This exception is raised by a decision procedure 
 * that was unable to provide an answer because it 
 * does not work any longer.
 * 
 * @author Pietro Braione
 *
 */
public class DecisionException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3336464743737235801L;

    public DecisionException() {
        super();
    }

	public DecisionException(String param) {
        super(param);
    }
    
	public DecisionException(Exception e) {
		super(e);
	}
}
