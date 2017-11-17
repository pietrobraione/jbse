package jbse.algo;

/**
 * Exception raised whenever an {@link Action} requires 
 * the engine to execute another (sequence of) {@link Action}s.
 * 
 * @author Pietro Braione
 *
 */
public final class ContinuationException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1986437767183635494L;

    private final Action[] continuation;

    public ContinuationException(Action... continuation) {
        this.continuation = continuation.clone(); //safety copy
    }

    public Action[] getContinuation() {
        return this.continuation;
    }
}
