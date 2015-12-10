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

    public AccessLocalVariable(String variableName) {
        this.variableName = variableName;
        this.toString = "{ROOT}:" + variableName;
    }
    
    public String variableName() {
        return this.variableName;
    }
    
    @Override
    public String toString() {
        return this.toString;
    }

}
