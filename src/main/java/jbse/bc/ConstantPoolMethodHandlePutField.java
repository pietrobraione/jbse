package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing a putfield kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandlePutField extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandlePutField(Signature value) {
    	super(value, 89);
    }

	@Override
	public int getKind() {
		return 3;
	}
}
