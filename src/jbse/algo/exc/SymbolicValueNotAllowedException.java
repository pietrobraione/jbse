package jbse.algo.exc;

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
