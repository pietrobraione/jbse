package jbse.common.exc;

/**
 * This exception is raised whenever happens an unexpected 
 * internal error, indicative of a bug in the implementation 
 * of JBSE.
 * 
 * @author Pietro Braione
 *
 */
public class UnexpectedInternalException extends RuntimeException {
    public UnexpectedInternalException() {
        super();
    }

    public UnexpectedInternalException(String message) {
        super(message);
    }

    public UnexpectedInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedInternalException(Throwable cause) {
        super(cause);
    }
	/**
	 * 
	 */
	private static final long serialVersionUID = -1281869981541838200L;

}
