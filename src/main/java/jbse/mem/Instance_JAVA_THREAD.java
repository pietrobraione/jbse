package jbse.mem;

/**
 * Class that represent an instance in the heap of an object 
 * whose class is {@code java.lang.Thread} or one of its subclasses. 
 */
public interface Instance_JAVA_THREAD extends Instance {
    /**
     * Returns whether the thread this
     * object represents is interrupted.
     * 
     * @return a {@code boolean}.
     */
    boolean isInterrupted();
    
    /**
     * Sets the interruption state of the
     * thread this object represents.
     * 
     * @param interrupted a {@code boolean}, 
     *        {@code true} iff the thread is 
     *        interrupted.
     */
    void setInterrupted(boolean interrupted);
    
    Instance_JAVA_THREAD clone();
}
