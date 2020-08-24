package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing a getstatic kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleGetStatic extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleGetStatic(Signature value) {
    	super(value, 61);
    }

	@Override
	public int getKind() {
		return 2;
	}
}
