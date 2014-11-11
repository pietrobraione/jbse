package jbse.mem;

import jbse.exc.mem.ValueDoesNotSupportNativeException;

/**
 * Class for references to the {@link Heap}, both concrete and 
 * symbolic.
 */
public abstract class Reference extends Value {
	Reference(char type) {
		super(type);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}

    @Override
    public Reference clone() {
    	return (Reference) super.clone();
    }
}