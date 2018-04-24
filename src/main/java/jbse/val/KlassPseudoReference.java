package jbse.val;

import jbse.mem.Klass;

/**
 * Class that represent a {@link ReferenceSymbolic} to a {@link Klass}. 
 * It cannot be really used as a reference, its only purpose is to be 
 * origin to {@link Klass} objects.
 */
public final class KlassPseudoReference extends ReferenceSymbolic {
    private final String className;
    
    /**
     * Constructor.
     * 
     * @param className a {@link String}, the name of a class.
     */
    KlassPseudoReference(String className) {
    	super(null, null);
    	this.className = className;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    @Override
    public String asOriginString() {
        return "[" + this.className + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        return result;
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
        final KlassPseudoReference other = (KlassPseudoReference) obj;
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
    public String toString() {
        return "[" + this.className + "]";
    }
}