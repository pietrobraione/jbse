package jbse.mem;

/**
 * Class that represents the shared portion of an object 
 * in the static method area, i.e., its static fields.
 */
public interface Klass extends Objekt {
    /**
     * Checks whether the initialization of this {@link Klass} 
     * is started, i.e., if the frame for its <clinit> method
     * was loaded on the state's stack.
     * 
     * @return {@code true} iff the initialization of this 
     *         {@link Klass} is started.
     */
    boolean initializationStarted();

    /**
     * Checks whether the initialization of this {@link Klass} 
     * is completed, i.e., if it is initialized.
     * 
     * @return {@code true} iff this {@link Klass} is initialized.
     */
    boolean initializationCompleted();

    /**
     * Sets this {@link Klass} to the
     * status where its initialization is started. 
     * After the invocation of this method an 
     * invocation to {@link #initializationStarted()} 
     * will return {@code true}.
     */
    void setInitializationStarted();

    /**
     * Sets this {@link Klass} to the
     * initialized status. After the 
     * invocation of this method an 
     * invocation to {@link #initializationStarted()} 
     * and {@link #initializationCompleted()} 
     * will return {@code true}.
     */
    void setInitializationCompleted();
    
    Klass clone();
}
