package jbse.mem;

/**
 * Class that represents the shared portion of an object 
 * in the static method area, i.e., its static fields.
 */
public interface Klass extends Objekt {
    /**
     * Checks whether this {@link Klass} is initialized.
     * 
     * @return {@code true} iff this {@link Klass} is initialized.
     */
    boolean isInitialized();

    /**
     * Sets this {@link Klass} to the
     * initialized status. After the 
     * invocation of this method an 
     * invocation to {@link #isInitialized()} 
     * will return {@code true}.
     */
    void setInitialized();
    
    Klass clone();
}
