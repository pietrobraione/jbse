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
    
    public static InterruptException mk(Algorithm<?, ?, ?, ?, ?> continuation) { 
        return new InterruptException(continuation); 
    }
    
    private Algorithm<?, ?, ?, ?, ?> continuation = null;
    
    public boolean hasContinuation() {
        return (this.continuation != null);
    }
    
    public Algorithm<?, ?, ?, ?, ?> getContinuation() {
        return this.continuation;
    }
    
    /**
     * Do not instantiate!
     */
    private InterruptException() { 
        this.continuation = null;
    }
    
    private InterruptException(Algorithm<?, ?, ?, ?, ?> continuation) { 
        this.continuation = continuation;
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 5164443391404129890L;
}
