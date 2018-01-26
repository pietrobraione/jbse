package jbse.bc.exc;

/**
 * Exception thrown in all the situations (e.g., class resolution)
 * where a class definition was found but it is ill-formed (not a classfile). 
 * Typically some {@link LinkageError} of {@link VerifyError} 
 * must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class ClassFileIllFormedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5384308551943899471L;

    public ClassFileIllFormedException() {
        super();
    }

    public ClassFileIllFormedException(String message) {
        super(message);
    }

    public ClassFileIllFormedException(Throwable cause) {
        super(cause);
    }

    public ClassFileIllFormedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassFileIllFormedException(String message, Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
