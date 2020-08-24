package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing an invokespecial kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleInvokeSpecial extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleInvokeSpecial(Signature value) {
    	super(value, 149);
    }

	@Override
	public int getKind() {
		return 7;
	}
}
