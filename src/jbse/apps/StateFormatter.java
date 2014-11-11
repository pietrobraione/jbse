package jbse.apps;

import jbse.exc.common.UnexpectedInternalException;
import jbse.mem.State;

/**
 * A state formatter.
 * 
 * @author Pietro Braione
 */
public interface StateFormatter {
	/**
	 * Formats a {@link State}.
	 * 
	 * @param s the {@link State} to be formatted.
	 * @throws UnexpectedInternalException 
	 */
	void format(State s) throws UnexpectedInternalException;
	
	/**
	 * Emits the formatted {@link State}.
	 */
	void emit();
	
	/**
	 * Cleans the current formatting. Must be invoked
	 * between two invocations of {@link #format(State)}
	 * (and typically after one or more invocations of
	 * {@link #emit()}.
	 */
	void cleanup();
}
