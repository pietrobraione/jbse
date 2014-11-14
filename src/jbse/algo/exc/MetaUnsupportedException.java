package jbse.algo.exc;

import jbse.meta.annotations.MetaOverridden;

/**
 * Exception raised whenever processing a {@link MetaOverridden} directive fails. 
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
