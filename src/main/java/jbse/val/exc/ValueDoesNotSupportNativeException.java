package jbse.val.exc;

import jbse.algo.exc.CannotInvokeNativeException;
import jbse.val.Value;

/**
 * Exception thrown when a {@link Value} cannot be represented metacircularly, 
 * because it does not exist (e.g., symbolic values) or it is to complex to 
 * do so (e.g., objects).
 * 
 * @author Pietro Braione
 *
 */
public class ValueDoesNotSupportNativeException extends CannotInvokeNativeException {
    public ValueDoesNotSupportNativeException() {
        super();
    }

    public ValueDoesNotSupportNativeException(String s) {
        super(s);
    }

	/**
	 * 
	 */
	private static final long serialVersionUID = -3772182382542414429L;

}
