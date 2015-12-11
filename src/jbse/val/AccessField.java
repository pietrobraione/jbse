package jbse.val;

/**
 * An access to an object's field.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessField extends AccessNonroot {
    private final String fieldName;
    private final int hashCode;

    public AccessField(String fieldName) {
        this.fieldName = fieldName;
        final int prime = 6271;
        this.hashCode = prime + ((this.fieldName == null) ? 0 : this.fieldName.hashCode());
    }
    
    public String fieldName() {
        return this.fieldName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AccessField other = (AccessField) obj;
        if (this.fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        } else if (!this.fieldName.equals(other.fieldName)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
}
