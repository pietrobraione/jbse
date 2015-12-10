package jbse.val;

/**
 * An access to the static method area for a class.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessStatic extends AccessRoot {
    private final String className;
    private final String toString;

    public AccessStatic(String className) {
        this.className = className;
        this.toString = "[" + className + "]";
    }
    
    public String className() {
        return this.className;
    }
    
    @Override
    public String toString() {
        return this.toString;
    }

}
