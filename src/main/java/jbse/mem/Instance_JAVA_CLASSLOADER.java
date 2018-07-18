package jbse.mem;

/**
 * Class that represent an instance in the heap of an object 
 * whose class is {@code java.lang.ClassLoader} or of one of its subclasses. 
 */
public interface Instance_JAVA_CLASSLOADER extends Instance {
    /**
     * Returns the identifier of this
     * {@code java.lang.ClassLoader}.
     * 
     * @return an {@code int}, the identifier of 
     * this classloader.
     */
    int classLoaderIdentifier();
    
    Instance_JAVA_CLASSLOADER clone();
}
