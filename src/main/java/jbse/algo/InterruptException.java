package jbse.algo;


/**
 * This exception is raised to interrupt the execution of an algorithm, 
 * for resuming it later (as happens with the execution of class initialization 
 * methods) or because of an unanticipated jump (as happens after a stack
 * unwind).
 * 
 * @author Pietro Braione
 *
 */
public class InterruptException extends Exception {
    public static InterruptException mk() { 
        return new InterruptException(); 
    }

    public static InterruptException mk(Action continuation) {
        return new InterruptException(continuation); 
    }

    private Action continuation = null;

    public boolean hasContinuation() {
        return (this.continuation != null);
    }

    public Action getContinuation() {
        return this.continuation;
    }

    /**
     * Do not instantiate directly!
     */
    private InterruptException() { 
        this.continuation = null;
    }

    /**
     * Do not instantiate directly!
     */
    private InterruptException(Action continuation) { 
        this.continuation = continuation;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5164443391404129890L;
}
