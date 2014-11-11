package jbse.mem;

import jbse.Type;
import jbse.exc.mem.ValueDoesNotSupportNativeException;

/**
 * Class representing a default value with unknown type. 
 * Used to initialize the local variable memory area to
 * cope with absence of type information. It is a singleton.
 * 
 * @author Pietro Braione
 */
public class DefaultValue extends Value {
    private static DefaultValue instance = new DefaultValue();
	
	private DefaultValue() { 
		super(Type.UNKNOWN); 
	}
	
    public static DefaultValue getInstance() {
        return DefaultValue.instance;
    }
	
	@Override
	public boolean equals(Object o) {
		return (o == instance);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public DefaultValue clone() {
		return DefaultValue.getInstance();
	}
	
	@Override
	public String toString() {
		return "<DEFAULT>";
	}

	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}

	@Override
	public boolean isSymbolic() {
		return true;
	}
}
