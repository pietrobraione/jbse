package jbse.mem.exc;

import jbse.mem.Array;

/**
 * Exception thrown whenever a method reserved for normalized {@link Array}s is
 * invoked on an {@link Array} that it is not normalized.
 * 
 * @author Pietro Braione
 *
 */
public class FastArrayAccessNotAllowedException extends Exception {
//TODO make it subclass of Exception
	/**
	 * 
	 */
	private static final long serialVersionUID = 1733842308807538335L;

}
