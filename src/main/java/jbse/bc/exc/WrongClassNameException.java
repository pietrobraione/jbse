package jbse.bc.exc;

/**
 * Exception thrown during class creation when a classfile 
 * has a wrong class name. 
 * Typically {@link NoClassDefFoundError} must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class WrongClassNameException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -4177356681298796900L;

    public WrongClassNameException() {
        super();
    }

    public WrongClassNameException(String message) {
        super(message);
    }

    public WrongClassNameException(Throwable cause) {
        super(cause);
    }

    public WrongClassNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongClassNameException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
