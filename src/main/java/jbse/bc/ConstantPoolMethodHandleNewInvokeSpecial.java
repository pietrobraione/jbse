package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing a new+invokespecial kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleNewInvokeSpecial extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleNewInvokeSpecial(Signature value) {
    	super(value, 173);
    }

	@Override
	public int getKind() {
		return 8;
	}
}
