package jbse.mem.exc;

/**
 * This exception is raised after an attempt to read a 
 * frame from an empty thread stack.
 * 
 * @author Pietro Braione
 *
 */
public final class ThreadStackEmptyException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 5837579638395469502L;
    
    public ThreadStackEmptyException() {
    	super();
    }
 
}
