package jbse.algo.exc;

/**
 * Exception thrown whenever a native method invocation fails.
 * 
 * @author Pietro Braione
 */
public abstract class CannotInvokeNativeException extends CannotManageStateException {
    /**
     * 
     */
    private static final long serialVersionUID = -4352182698283195143L;

    public CannotInvokeNativeException() { 
        super(); 
    }

    public CannotInvokeNativeException(String s) { 
        super(s); 
    }

    public CannotInvokeNativeException(Throwable e) { 
        super(e); 
    }

    public CannotInvokeNativeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotInvokeNativeException(String message, Throwable cause,
                                       boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
