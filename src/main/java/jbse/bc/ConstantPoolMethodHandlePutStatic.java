package jbse.bc;

/**
 * A {@link ConstantPoolMethodHandle} representing a putstatic kind. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolMethodHandlePutStatic extends ConstantPoolMethodHandle {
    public ConstantPoolMethodHandlePutStatic(Signature value) {
    	super(value, 101);
    }

	@Override
	public int getKind() {
		return 4;
	}
}
