package jbse.val;

import jbse.common.Type;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Class for references to the {@link Heap}, both concrete and 
 * symbolic.
 */
public abstract class Reference extends Value {
	protected Reference(char type) {
		super(type);
	}
	
    protected Reference() {
        this(Type.REFERENCE);
    }


    /**
     * {@inheritDoc}
     */
	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}
}