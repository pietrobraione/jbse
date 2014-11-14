package jbse.algo.exc;

import jbse.meta.annotations.Uninterpreted;

/**
 * Exception raised whenever processing a {@link Uninterpreted} directive fails. 
 *  
 * @author Pietro Braione
 *
 */
public class UninterpretedUnsupportedException extends CannotManageStateException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5130327911662679391L;
	
	public UninterpretedUnsupportedException(String param) {
		super(param);
	}

}
