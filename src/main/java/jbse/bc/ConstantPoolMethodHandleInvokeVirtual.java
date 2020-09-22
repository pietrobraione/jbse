package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing an invokevirtual kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleInvokeVirtual extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleInvokeVirtual(Signature value) {
    	super(value, 107);
    }

	@Override
	public int getKind() {
		return 5;
	}
}
