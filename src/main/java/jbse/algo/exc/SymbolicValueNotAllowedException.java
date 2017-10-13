package jbse.algo.exc;

/**
 * Exception thrown whenever a parameter has a symbolic value in a 
 * context where that parameter must have a concrete value, usually
 * because of some limitation of JBSE.
 * 
 * @author Pietro Braione
 */
public class SymbolicValueNotAllowedException extends CannotInvokeNativeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6739734862012429576L;

    public SymbolicValueNotAllowedException() {
    }

    public SymbolicValueNotAllowedException(String message) {
        super(message);
    }

    public SymbolicValueNotAllowedException(Throwable cause) {
        super(cause);
    }

    public SymbolicValueNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SymbolicValueNotAllowedException(String message, Throwable cause,
                                            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
