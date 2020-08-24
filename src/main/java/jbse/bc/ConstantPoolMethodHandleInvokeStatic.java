package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing an invokestatic kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleInvokeStatic extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleInvokeStatic(Signature value) {
    	super(value, 127);
    }

	@Override
	public int getKind() {
		return 6;
	}
}
