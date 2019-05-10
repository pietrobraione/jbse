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
	private static final InterruptException INSTANCE = new InterruptException();
    public static InterruptException mk() { 
    	INSTANCE.continuation = null;
        return INSTANCE; 
    }

    public static InterruptException mk(Action continuation) {
    	INSTANCE.continuation = continuation;
        return INSTANCE; 
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
     * 
     */
    private static final long serialVersionUID = 5164443391404129890L;
}
