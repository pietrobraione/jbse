package jbse.mem.exc;

import jbse.common.exc.InvalidInputException;
import jbse.mem.State;

/** 
 * Exception thrown whenever one attempts to mutate a frozen {@link State}.
 * 
 * @author Pietro Braione
 *
 */
public class FrozenStateException extends InvalidInputException {

	public FrozenStateException() {
		super("Attempted to modify a frozen state.");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3602611805258952393L;

}
