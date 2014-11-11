package jbse.exc.algo;

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
}
