package jbse.mem.exc;

/**
 * This exception is raised whenever an attempt to put an object
 * in the heap fails because the heap is full.
 * 
 * @author Pietro Braione
 *
 */
public final class HeapMemoryExhaustedException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -421633905697393489L;

    public HeapMemoryExhaustedException() {
    	super();
    }
 
}
