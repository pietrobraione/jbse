package jbse.val;

import jbse.bc.ClassFile;

/**
 * An access to the static method area for a class.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessStatic extends AccessRoot {
    private final ClassFile classFile;
    private final String toString;
    private final int hashCode;

    public AccessStatic(ClassFile classFile) {
        this.classFile = classFile;
        this.toString = "[" + classFile.getClassName().replace('/', '.').replace('$', '.') + "]";
        final int prime = 7331;
        this.hashCode = prime + ((this.classFile == null) ? 0 : this.classFile.hashCode());
    }
    
    public ClassFile classFile() {
        return this.classFile;
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
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
