package jbse.bc.exc;

/**
 * Exception thrown during class definition whenever one
 * tries to redefine an already loaded class.
 * Typically {@link LinkageError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class AlreadyDefinedClassException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 4020182817018905954L;

    public AlreadyDefinedClassException() {
        super();
    }

    public AlreadyDefinedClassException(String message) {
        super(message);
    }

    public AlreadyDefinedClassException(Throwable cause) {
        super(cause);
    }

    public AlreadyDefinedClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyDefinedClassException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
