package jbse.val;

import jbse.common.Type;

/**
 * Class representing a default value with unknown type. 
 * Used to initialize the local variable memory area to
 * cope with absence of type information. It is a singleton.
 * 
 * @author Pietro Braione
 */
public final class DefaultValue extends Value {
    private static DefaultValue instance = new DefaultValue();

    private DefaultValue() { 
        super(Type.UNKNOWN); 
    }

    public static DefaultValue getInstance() {
        return instance;
    }

    @Override
    public boolean isSymbolic() {
        return true;
    }
    
    @Override
    public void accept(ValueVisitor v) throws Exception {
    	v.visitDefaultValue(this);
    }

    @Override
    public String toString() {
        return "<DEFAULT>";
    }

    @Override
    public boolean equals(Object o) {
        return (o == instance);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

