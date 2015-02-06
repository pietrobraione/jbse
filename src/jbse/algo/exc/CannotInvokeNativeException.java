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

	/**
	 * 
	 */
	private static final long serialVersionUID = -4352182698283195143L;

}
