package jbse.rewr.exc;

/**
 * Thrown when rewriting does not produce a result.
 * 
 * @author Pietro Braione
 *
 */
public class NoResultException extends Exception {
	public NoResultException() {
		super();
	}
	
	public NoResultException(Throwable e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -750854451137634121L;

}
