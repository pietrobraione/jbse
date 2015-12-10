package jbse.val;

/**
 * An access to an object's field.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessField extends AccessNonroot {
    private final String fieldName;

    public AccessField(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String fieldName() {
        return this.fieldName;
    }
    
    @Override
    public String toString() {
        return this.fieldName;
    }

}
