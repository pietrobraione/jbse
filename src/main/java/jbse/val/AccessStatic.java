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
    private final int hashCode;

    public AccessStatic(String className) {
        this.className = className;
        this.toString = "[" + className + "]";
        final int prime = 7331;
        this.hashCode = prime + ((this.className == null) ? 0 : this.className.hashCode());
    }
    
    public String className() {
        return this.className;
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
        final AccessStatic other = (AccessStatic) obj;
        if (this.className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!this.className.equals(other.className)) {
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
