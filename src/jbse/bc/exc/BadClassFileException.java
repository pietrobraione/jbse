package jbse.bc.exc;

public abstract class BadClassFileException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 4687202277217446639L;

    public BadClassFileException() {
        //nothing to do
    }

    public BadClassFileException(String message) {
        super(message);
    }

    public BadClassFileException(Throwable cause) {
        super(cause);
    }

    public BadClassFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadClassFileException(String message, Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
