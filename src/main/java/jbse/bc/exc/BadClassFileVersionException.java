package jbse.bc.exc;

/**
 * Exception thrown during class creation when a classfile 
 * has a wrong version number. 
 * Typically {@link UnsupportedClassVersionError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class BadClassFileVersionException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -8946789740113001375L;

    public BadClassFileVersionException() {
        super();
    }

    public BadClassFileVersionException(String message) {
        super(message);
    }

    public BadClassFileVersionException(Throwable cause) {
        super(cause);
    }

    public BadClassFileVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadClassFileVersionException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
