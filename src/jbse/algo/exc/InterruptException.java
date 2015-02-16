package jbse.algo.exc;

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

    /**
     * 
     */
    private static final long serialVersionUID = 5164443391404129890L;
}
