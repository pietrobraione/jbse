package jbse.val;

/**
 * An access to a local variable.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessLocalVariable extends AccessRoot {
    private final String variableName;
    private final String toString;
    private final int hashCode;

    public AccessLocalVariable(String variableName) {
        this.variableName = variableName;
        this.toString = "{ROOT}:" + variableName;
        final int prime = 4561;
        this.hashCode = prime + ((this.variableName == null) ? 0 : this.variableName.hashCode());
    }
    
    public String variableName() {
        return this.variableName;
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
        final AccessLocalVariable other = (AccessLocalVariable) obj;
        if (this.variableName == null) {
            if (other.variableName != null) {
                return false;
            }
        } else if (!this.variableName.equals(other.variableName)) {
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
