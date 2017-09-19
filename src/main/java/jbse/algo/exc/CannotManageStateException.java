package jbse.algo.exc;

/**
 * This exception is raised whenever the engine is not able to 
 * handle a state because of the current limitations of the engine.
 * 
 * @author Pietro Braione
 */
public abstract class CannotManageStateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 48445177324954673L;

	public CannotManageStateException(String param) {
		super(param);
	}

	public CannotManageStateException() {
		super();
	}
	
	public CannotManageStateException(Throwable e) {
		super(e);
	}

    public CannotManageStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotManageStateException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
