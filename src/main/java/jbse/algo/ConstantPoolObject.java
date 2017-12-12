package jbse.algo;

import jbse.bc.ConstantPoolValue;
import jbse.val.Reference;

/**
 * A {@link ConstantPoolValue} representing an arbitrary object. 
 * It is used with anonymous classes having patched constant pool values.
 * 
 * @author Pietro Braione
 */
public final class ConstantPoolObject extends ConstantPoolValue {
    private final Reference value;
    private final int hashCode;

    public ConstantPoolObject(Reference value) { 
        this.value = value; 
        final int prime = 881;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        this.hashCode = result;
    }

    @Override
    public Reference getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
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
        final ConstantPoolObject other = (ConstantPoolObject) obj;
        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
