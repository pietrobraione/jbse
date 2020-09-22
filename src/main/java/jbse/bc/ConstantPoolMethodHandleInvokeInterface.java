package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing an invokeinterface kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleInvokeInterface extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleInvokeInterface(Signature value) {
    	super(value, 193);
    }

	@Override
	public int getKind() {
		return 9;
	}
}
