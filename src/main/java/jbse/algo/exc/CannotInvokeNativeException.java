package jbse.algo.exc;


public class CannotInvokeNativeException extends CannotManageStateException {
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

    /**
	 * 
	 */
	private static final long serialVersionUID = -4352182698283195143L;

}
