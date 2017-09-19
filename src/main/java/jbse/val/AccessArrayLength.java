package jbse.val;

/**
 * An access to an object's field.
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
