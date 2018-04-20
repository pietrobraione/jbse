package jbse.val;

import jbse.bc.ClassFile;
import jbse.mem.Klass;

/**
 * Class that represent a {@link ReferenceSymbolic} to a {@link Klass}. 
 * It cannot be really used as a reference, its only purpose is to be 
 * origin to {@link Klass} objects.
 */
public final class KlassPseudoReference extends ReferenceSymbolic {
    private final ClassFile classFile;
    
    /**
     * Constructor.
     * 
     * @param classFile the {@link ClassFile} for the {@link Klass} to be referred.
     */
    KlassPseudoReference(ClassFile classFile) {
    	super(null);
    	this.classFile = classFile;
    }
    
    public ClassFile getClassFile() {
        return this.classFile;
    }
    
    @Override
    public String asOriginString() {
        return "[" + this.classFile.toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.classFile == null) ? 0 : this.classFile.hashCode());
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
        if (this.classFile == null) {
            if (other.classFile != null) {
                return false;
            }
        } else if (!this.classFile.equals(other.classFile)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + this.classFile.toString() + "]";
    }
}