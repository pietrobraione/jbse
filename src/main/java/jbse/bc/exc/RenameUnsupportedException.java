package jbse.bc.exc;

/**
 * Exception thrown if a classfile cannot be renamed. 
 * 
 * @author Pietro Braione
 *
 */
public class RenameUnsupportedException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2678677154854773941L;

	public RenameUnsupportedException() {
        super();
    }

    public RenameUnsupportedException(String message) {
        super(message);
    }

    public RenameUnsupportedException(Throwable cause) {
        super(cause);
    }

    public RenameUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenameUnsupportedException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
