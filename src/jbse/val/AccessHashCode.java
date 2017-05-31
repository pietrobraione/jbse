package jbse.val;

/**
 * An access to an object's field.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessHashCode extends AccessNonroot {
    private static final AccessHashCode INSTANCE = new AccessHashCode();
    
    private AccessHashCode() {
        //nothing to do
    }
    
    public static AccessHashCode instance() {
        return INSTANCE;
    }
    
    @Override
    public String toString() {
        return "hashCode()";
    }
}
