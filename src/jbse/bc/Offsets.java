package jbse.bc;

/**
 * Constants for all bytecode program counter offsets.
 * 
 * @author Pietro Braione
 */
public final class Offsets {
	public static final int XCONST_OFFSET = 1;
	public static final int XALOADSTORE_OFFSET = 1;
	public static final int XLOADSTORE_WIDE_OFFSET = 3;
	public static final int XLOADSTORE_IMMEDIATE_OFFSET = 2;
	public static final int XLOADSTORE_OPCODE_OFFSET = 1;
	public static final int NEWARRAY_OFFSET = 2;
	public static final int ANEWARRAY_OFFSET = 3;
	public static final int MULTIANEWARRAY_OFFSET = 4;
	public static final int ARRAYLENGTH_OFFSET = 1;
	public static final int INVOKESPECIALSTATICVIRTUAL_OFFSET = 3;
	public static final int INVOKEDYNAMICINTERFACE_OFFSET = 5;
	
	/**
	 * Do not instantiate it!
	 */
	private Offsets() { }
}
