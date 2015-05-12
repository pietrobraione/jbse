package jbse.bc;

/**
 * Constants for all bytecode program counter offsets.
 * 
 * @author Pietro Braione
 */
public final class Offsets {
    public static final int ANEWARRAY_OFFSET = 3;
    public static final int ARRAYLENGTH_OFFSET = 1;
    public static final int BIPUSH_OFFSET = 2;
    public static final int CASTINSTANCEOF_OFFSET = 3;
    public static final int DUP_OFFSET = 1;
    public static final int GETX_PUTX_OFFSET = 3;
    public static final int IF_ACMPX_XNULL_OFFSET = 3;
    public static final int IFX_OFFSET = 3;
    public static final int IINC_OFFSET = 3;
    public static final int IINC_WIDE_OFFSET = 5;
    public static final int INVOKEDYNAMICINTERFACE_OFFSET = 5;
    public static final int INVOKESPECIALSTATICVIRTUAL_OFFSET = 3;
    public static final int JSR_OFFSET = 3;
    public static final int JSR_W_OFFSET = 5;
    public static final int LDC_OFFSET = 2;
    public static final int LDC_W_OFFSET = 3;
    public static final int MATH_LOGICAL_OP_OFFSET = 1;
    public static final int MONITORX_OFFSET = 1;
    public static final int MULTIANEWARRAY_OFFSET = 4;
    public static final int NEW_OFFSET = 3;
	public static final int NEWARRAY_OFFSET = 2;
    public static final int NOP_OFFSET = 1;
    public static final int POP_OFFSET = 1;
    public static final int SIPUSH_OFFSET = 3;
    public static final int SWAP_OFFSET = 1;
    public static final int WIDE_OFFSET = 1;
    public static final int X2Y_OFFSET = 1;
    public static final int XALOADSTORE_OFFSET = 1;
    public static final int XCMPY_OFFSET = 1;
    public static final int XCONST_OFFSET = 1;
    public static final int XLOADSTORE_IMMEDIATE_OFFSET = 2;
    public static final int XLOADSTORE_IMMEDIATE_WIDE_OFFSET = 3;
    public static final int XLOADSTORE_IMPLICIT_OFFSET = 1;
    
	/**
	 * Do not instantiate it!
	 */
	private Offsets() { }
}
