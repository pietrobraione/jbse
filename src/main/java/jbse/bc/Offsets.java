package jbse.bc;

import static jbse.bc.Opcodes.*;

/**
 * Constants for all bytecode program counter offsets.
 * 
 * @author Pietro Braione
 */
public final class Offsets {
    public static final int ANEWARRAY_OFFSET = 3;
    public static final int ARRAYLENGTH_OFFSET = 1;
    public static final int ATHROW_OFFSET = 1;
    public static final int BIPUSH_OFFSET = 2;
    public static final int CASTINSTANCEOF_OFFSET = 3;
    public static final int DUP_OFFSET = 1;
    public static final int GETX_PUTX_OFFSET = 3;
    public static final int GOTO_OFFSET = 3;
    public static final int GOTO_W_OFFSET = 5;
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
    public static final int RET_OFFSET = 2;
    public static final int RET_WIDE_OFFSET = 3;
    public static final int RETURN_OFFSET = 1;
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
    
    public static int offsetInvoke(boolean isInterface) {
        return isInterface ? INVOKEDYNAMICINTERFACE_OFFSET : INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
    
    public static int offset(byte[] code, int currentPC, int previousPC) {
        final boolean wide = (previousPC <= 0 ? false : code[previousPC] == OP_WIDE);
        switch (code[currentPC]) {
        case OP_NOP:
            return NOP_OFFSET;
        case OP_ACONST_NULL:
        case OP_ICONST_M1:
        case OP_ICONST_0:
        case OP_ICONST_1:
        case OP_ICONST_2:
        case OP_ICONST_3:
        case OP_ICONST_4:
        case OP_ICONST_5:
        case OP_LCONST_0:
        case OP_LCONST_1:
        case OP_FCONST_0:
        case OP_FCONST_1:
        case OP_FCONST_2:
        case OP_DCONST_0:
        case OP_DCONST_1:
            return XCONST_OFFSET;
        case OP_BIPUSH:
            return BIPUSH_OFFSET;
        case OP_SIPUSH:
            return SIPUSH_OFFSET;
        case OP_LDC:
            return LDC_OFFSET;
        case OP_LDC_W:
        case OP_LDC2_W:
            return LDC_W_OFFSET;
        case OP_ILOAD:
        case OP_LLOAD:
        case OP_FLOAD:
        case OP_DLOAD:
        case OP_ALOAD:
        case OP_ISTORE:
        case OP_LSTORE:
        case OP_FSTORE:
        case OP_DSTORE:
        case OP_ASTORE:
            return (wide ? XLOADSTORE_IMMEDIATE_WIDE_OFFSET : XLOADSTORE_IMMEDIATE_OFFSET);
        case OP_ILOAD_0:
        case OP_ILOAD_1:
        case OP_ILOAD_2:
        case OP_ILOAD_3:
        case OP_LLOAD_0:
        case OP_LLOAD_1:
        case OP_LLOAD_2:
        case OP_LLOAD_3:
        case OP_FLOAD_0:
        case OP_FLOAD_1:
        case OP_FLOAD_2:
        case OP_FLOAD_3:
        case OP_DLOAD_0:
        case OP_DLOAD_1:
        case OP_DLOAD_2:
        case OP_DLOAD_3:
        case OP_ALOAD_0:
        case OP_ALOAD_1:
        case OP_ALOAD_2:
        case OP_ALOAD_3:
        case OP_ISTORE_0:
        case OP_ISTORE_1:
        case OP_ISTORE_2:
        case OP_ISTORE_3:
        case OP_LSTORE_0:
        case OP_LSTORE_1:
        case OP_LSTORE_2:
        case OP_LSTORE_3:
        case OP_FSTORE_0:
        case OP_FSTORE_1:
        case OP_FSTORE_2:
        case OP_FSTORE_3:
        case OP_DSTORE_0:
        case OP_DSTORE_1:
        case OP_DSTORE_2:
        case OP_DSTORE_3:
        case OP_ASTORE_0:
        case OP_ASTORE_1:
        case OP_ASTORE_2:
        case OP_ASTORE_3:
            return XLOADSTORE_IMPLICIT_OFFSET;
        case OP_IALOAD:
        case OP_LALOAD:
        case OP_FALOAD:
        case OP_DALOAD:
        case OP_AALOAD:
        case OP_BALOAD:
        case OP_CALOAD:
        case OP_SALOAD:
        case OP_IASTORE:
        case OP_LASTORE:
        case OP_FASTORE:
        case OP_DASTORE:
        case OP_AASTORE:
        case OP_BASTORE:
        case OP_CASTORE:
        case OP_SASTORE:
            return XALOADSTORE_OFFSET;
        case OP_POP:
        case OP_POP2:
            return POP_OFFSET;
        case OP_DUP:
        case OP_DUP_X1:
        case OP_DUP_X2:
        case OP_DUP2:
        case OP_DUP2_X1:
        case OP_DUP2_X2:
            return DUP_OFFSET;
        case OP_SWAP:
            return SWAP_OFFSET;
        case OP_IADD:
        case OP_LADD:
        case OP_FADD:
        case OP_DADD:
        case OP_ISUB:
        case OP_LSUB:
        case OP_FSUB:
        case OP_DSUB:
        case OP_IMUL:
        case OP_LMUL:
        case OP_FMUL:
        case OP_DMUL:
        case OP_IDIV:
        case OP_LDIV:
        case OP_FDIV:
        case OP_DDIV:
        case OP_IREM:
        case OP_LREM:
        case OP_FREM:
        case OP_DREM:
        case OP_INEG:
        case OP_LNEG:
        case OP_FNEG:
        case OP_DNEG:
        case OP_ISHL:
        case OP_LSHL:
        case OP_ISHR:
        case OP_LSHR:
        case OP_IUSHR:
        case OP_LUSHR:
        case OP_IAND:
        case OP_LAND:
        case OP_IOR:
        case OP_LOR:
        case OP_IXOR:
        case OP_LXOR:
            return MATH_LOGICAL_OP_OFFSET;
        case OP_IINC:
            return (wide ? IINC_WIDE_OFFSET : IINC_OFFSET);
        case OP_I2L:
        case OP_I2F:
        case OP_I2D:
        case OP_L2I:
        case OP_L2F:
        case OP_L2D:
        case OP_F2I:
        case OP_F2L:
        case OP_F2D:
        case OP_D2I:
        case OP_D2L:
        case OP_D2F:
        case OP_I2B:
        case OP_I2C:
        case OP_I2S:
            return X2Y_OFFSET;
        case OP_LCMP:
        case OP_FCMPL:
        case OP_FCMPG:
        case OP_DCMPL:
        case OP_DCMPG:
            return XCMPY_OFFSET;
        case OP_IFEQ:
        case OP_IFNE:
        case OP_IFLT:
        case OP_IFGE:
        case OP_IFGT:
        case OP_IFLE:
        case OP_IF_ICMPEQ:
        case OP_IF_ICMPNE:
        case OP_IF_ICMPLT:
        case OP_IF_ICMPGE:
        case OP_IF_ICMPGT:
        case OP_IF_ICMPLE:
            return IFX_OFFSET;
        case OP_IF_ACMPEQ:
        case OP_IF_ACMPNE:
        case OP_IFNULL:
        case OP_IFNONNULL:
            return IF_ACMPX_XNULL_OFFSET;
        case OP_GOTO:
            return GOTO_OFFSET;
        case OP_JSR:
            return JSR_OFFSET;
        case OP_RET:
            return (wide ? RET_WIDE_OFFSET : RET_OFFSET);
        //TODO tableswitch and lookupswitch
        case OP_IRETURN:
        case OP_LRETURN:
        case OP_FRETURN:
        case OP_DRETURN:
        case OP_ARETURN:
        case OP_RETURN:
            return RETURN_OFFSET;
        case OP_GETSTATIC:
        case OP_PUTSTATIC:
        case OP_GETFIELD:
        case OP_PUTFIELD:
            return GETX_PUTX_OFFSET;
        case OP_INVOKEVIRTUAL:
        case OP_INVOKESPECIAL:
        case OP_INVOKESTATIC:
            return INVOKESPECIALSTATICVIRTUAL_OFFSET;
        case OP_INVOKEINTERFACE:
        case OP_INVOKEDYNAMIC:
            return INVOKEDYNAMICINTERFACE_OFFSET;
        case OP_NEW:
            return NEW_OFFSET;
        case OP_NEWARRAY:
            return NEWARRAY_OFFSET;
        case OP_ANEWARRAY:
            return ANEWARRAY_OFFSET;
        case OP_ARRAYLENGTH:
            return ARRAYLENGTH_OFFSET;
        case OP_ATHROW:
            return ATHROW_OFFSET;
        case OP_CHECKCAST:
        case OP_INSTANCEOF:
            return CASTINSTANCEOF_OFFSET;
        case OP_MONITORENTER:
        case OP_MONITOREXIT:
            return MONITORX_OFFSET;
        case OP_WIDE:
            return WIDE_OFFSET;
        case OP_MULTIANEWARRAY:
            return MULTIANEWARRAY_OFFSET;
        case OP_GOTO_W:
            return GOTO_W_OFFSET;
        case OP_JSR_W:
            return JSR_W_OFFSET;
        //TODO breakpoint and invokehandle
        default:
            return 0;
        }
    }

    /**
     * Do not instantiate it!
     */
    private Offsets() { 
        throw new AssertionError();
    }
}
