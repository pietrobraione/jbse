package jbse.bc;

/**
 * A {@link ConstantPoolValue} representing a symbolic reference to a method handle from 
 * the constant pool. 
 * 
 * @author Pietro Braione
 *
 */
public abstract class ConstantPoolMethodHandle extends ConstantPoolValue {
    private final Signature value;
    private final int hashCode;

    ConstantPoolMethodHandle(Signature value, int prime) { 
        this.value = value; 
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        this.hashCode = result;
    }
    
    public abstract int getKind();

    @Override
    public final Signature getValue() {
        return this.value;
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConstantPoolMethodHandle other = (ConstantPoolMethodHandle) obj;
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
    public final String toString() {
        return this.value.toString();
    }
}
