package jbse.algo.exc;

/**
 * Exception raised whenever the meta-overriding of 
 * a method fails. 
 *  
 * @author Pietro Braione
 *
 */
public class MetaUnsupportedException extends CannotManageStateException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1826801410249226491L;
	
	public MetaUnsupportedException(String param) {
		super(param);
	}
}
