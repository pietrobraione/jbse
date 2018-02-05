package jbse.val;

/**
 * An access to an array's length.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessArrayLength extends AccessNonroot {
    private static final AccessArrayLength INSTANCE = new AccessArrayLength();
    
    private AccessArrayLength() {
        //nothing to do
    }
    
    public static AccessArrayLength instance() {
        return INSTANCE;
    }
    
    @Override
    public String toString() {
        return "length";
    }
}
