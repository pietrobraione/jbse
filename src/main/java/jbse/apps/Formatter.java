package jbse.apps;

import jbse.mem.State;

/**
 * A formatter for a single symbolic execution.
 * Not guaranteed to work across multiple 
 * symbolic executions.
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
     * Emits the formatted text. Must be invoked
     * after invocations of {@link #formatPrologue()},
     * {@link #formatState(State)}, and  {@link #formatEpilogue()}.
     */
    String emit();

    /**
     * Cleans the current formatting. Must be invoked
     * before invocations of {@link #formatPrologue()},
     * {@link #formatState(State)}, and  {@link #formatEpilogue()}
     * (typically after an invocation of {@link #emit()}).
     */
    void cleanup();
}
