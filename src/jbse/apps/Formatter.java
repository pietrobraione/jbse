package jbse.apps;

import jbse.mem.State;

/**
 * A formatter for symbolic execution.
 * 
 * @author Pietro Braione
 */
public interface Formatter {
    /** 
     * Formats a (possible) prologue. 
     */
    default void formatPrologue() { }
    
	/**
	 * Formats a {@link State}.
	 * 
	 * @param s the {@link State} to be formatted.
	 */
	void formatState(State s);
	
    /** 
     * Formats a (possible) epilogue. 
     */
    default void formatEpilogue() { }
    
	/**
	 * Emits the formatted {@link State}.
	 */
	void emit();
	
	/**
	 * Cleans the current formatting. Must be invoked
	 * between two invocations of {@link #formatState(State)}
	 * (and typically after one or more invocations of
	 * {@link #emit()}.
	 */
	void cleanup();
}
