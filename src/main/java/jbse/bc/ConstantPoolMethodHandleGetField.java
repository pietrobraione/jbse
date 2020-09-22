package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing a getfield kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandleGetField extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandleGetField(Signature value) {
    	super(value, 43);
    }

	@Override
	public int getKind() {
		return 1;
	}
}
