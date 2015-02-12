package jbse.bc.exc;

public class ClassFileIllFormedException extends BadClassFileException {

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
