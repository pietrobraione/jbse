package jbse.val;

/**
 * An access to an array member through an index.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessArrayMember extends AccessNonroot {
    private final Primitive index;
    private final String toString;
    private final int hashCode;

    public AccessArrayMember(Primitive index) {
        this.index = index;
        this.toString = "[" + index.toString() + "]";
        final int prime = 5903;
        this.hashCode = prime + ((this.index == null) ? 0 : this.index.hashCode());
    }
    
    public Primitive index() {
        return this.index;
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
        final AccessArrayMember other = (AccessArrayMember) obj;
        if (this.index == null) {
            if (other.index != null) {
                return false;
            }
        } else if (!this.index.equals(other.index)) {
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
        return this.toString;
    }
}
