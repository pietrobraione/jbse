package jbse.bc.exc;

/**
 * Exception thrown during class creation/loading to require 
 * an algorithm to upcall the JVM and load a class via a 
 * user-defined classloader.
 * 
 * @author Pietro Braione
 *
 */
public class PleaseLoadClassException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 5977494616930897048L;
    
    private final int initiatingLoader;
    private final String className;

    public PleaseLoadClassException(int initiatingLoader, String className) {
        this.initiatingLoader = initiatingLoader;
        this.className = className;
    }
    
    public int getInitiatingLoader() {
        return this.initiatingLoader;
    }
    
    public String className() {
        return this.className;
    }
}
