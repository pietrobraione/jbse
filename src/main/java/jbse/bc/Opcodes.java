package jbse.bc;

/**
 * Auxiliary opcodes stuff.
 * 
 * @author Pietro Braione
 * @author unknown
 *
 */
public final class Opcodes {
	//defined by the JVM specs
	public static final byte OP_NOP = 0;
	public static final byte OP_ACONST_NULL = 1;
	public static final byte OP_ICONST_M1 = 2;
	public static final byte OP_ICONST_0 = 3;
	public static final byte OP_ICONST_1 = 4;
	public static final byte OP_ICONST_2 = 5;
	public static final byte OP_ICONST_3 = 6;
	public static final byte OP_ICONST_4 = 7;
	public static final byte OP_ICONST_5 = 8;
	public static final byte OP_LCONST_0 = 9;
	public static final byte OP_LCONST_1 = 10;
	public static final byte OP_FCONST_0 = 11;
	public static final byte OP_FCONST_1 = 12;
	public static final byte OP_FCONST_2 = 13;
	public static final byte OP_DCONST_0 = 14;
	public static final byte OP_DCONST_1 = 15;
	public static final byte OP_BIPUSH = 16;
	public static final byte OP_SIPUSH = 17;
	public static final byte OP_LDC = 18;
	public static final byte OP_LDC_W = 19;
	public static final byte OP_LDC2_W = 20;
	public static final byte OP_ILOAD = 21;
	public static final byte OP_LLOAD = 22;
	public static final byte OP_FLOAD = 23;
	public static final byte OP_DLOAD = 24;
	public static final byte OP_ALOAD = 25;
	public static final byte OP_ILOAD_0 = 26;
	public static final byte OP_ILOAD_1 = 27;
	public static final byte OP_ILOAD_2 = 28;
	public static final byte OP_ILOAD_3 = 29;
	public static final byte OP_LLOAD_0 = 30;
	public static final byte OP_LLOAD_1 = 31;
	public static final byte OP_LLOAD_2 = 32;
	public static final byte OP_LLOAD_3 = 33;
	public static final byte OP_FLOAD_0 = 34;
	public static final byte OP_FLOAD_1 = 35;
	public static final byte OP_FLOAD_2 = 36;
	public static final byte OP_FLOAD_3 = 37;
	public static final byte OP_DLOAD_0 = 38;
	public static final byte OP_DLOAD_1 = 39;
	public static final byte OP_DLOAD_2 = 40;
	public static final byte OP_DLOAD_3 = 41;
	public static final byte OP_ALOAD_0 = 42;
	public static final byte OP_ALOAD_1 = 43;
	public static final byte OP_ALOAD_2 = 44;
	public static final byte OP_ALOAD_3 = 45;
	public static final byte OP_IALOAD = 46;
	public static final byte OP_LALOAD = 47;
	public static final byte OP_FALOAD = 48;
	public static final byte OP_DALOAD = 49;
	public static final byte OP_AALOAD = 50;
	public static final byte OP_BALOAD = 51;
	public static final byte OP_CALOAD = 52;
	public static final byte OP_SALOAD = 53;
	public static final byte OP_ISTORE = 54;
	public static final byte OP_LSTORE = 55;
	public static final byte OP_FSTORE = 56;
	public static final byte OP_DSTORE = 57;
	public static final byte OP_ASTORE = 58;
	public static final byte OP_ISTORE_0 = 59;
	public static final byte OP_ISTORE_1 = 60;
	public static final byte OP_ISTORE_2 = 61;
	public static final byte OP_ISTORE_3 = 62;
	public static final byte OP_LSTORE_0 = 63;
	public static final byte OP_LSTORE_1 = 64;
	public static final byte OP_LSTORE_2 = 65;
	public static final byte OP_LSTORE_3 = 66;
	public static final byte OP_FSTORE_0 = 67;
	public static final byte OP_FSTORE_1 = 68;
	public static final byte OP_FSTORE_2 = 69;
	public static final byte OP_FSTORE_3 = 70;
	public static final byte OP_DSTORE_0 = 71;
	public static final byte OP_DSTORE_1 = 72;
	public static final byte OP_DSTORE_2 = 73;
	public static final byte OP_DSTORE_3 = 74;
	public static final byte OP_ASTORE_0 = 75;
	public static final byte OP_ASTORE_1 = 76;
	public static final byte OP_ASTORE_2 = 77;
	public static final byte OP_ASTORE_3 = 78;
	public static final byte OP_IASTORE = 79;
	public static final byte OP_LASTORE = 80;
	public static final byte OP_FASTORE = 81;
	public static final byte OP_DASTORE = 82;
	public static final byte OP_AASTORE = 83;
	public static final byte OP_BASTORE = 84;
	public static final byte OP_CASTORE = 85;
	public static final byte OP_SASTORE = 86;
	public static final byte OP_POP = 87;
	public static final byte OP_POP2 = 88;
	public static final byte OP_DUP = 89;
	public static final byte OP_DUP_X1 = 90;
	public static final byte OP_DUP_X2 = 91;
	public static final byte OP_DUP2 = 92;
	public static final byte OP_DUP2_X1 = 93;
	public static final byte OP_DUP2_X2 = 94;
	public static final byte OP_SWAP = 95;
	public static final byte OP_IADD = 96;
	public static final byte OP_LADD = 97;
	public static final byte OP_FADD = 98;
	public static final byte OP_DADD = 99;
	public static final byte OP_ISUB = 100;
	public static final byte OP_LSUB = 101;
	public static final byte OP_FSUB = 102;
	public static final byte OP_DSUB = 103;
	public static final byte OP_IMUL = 104;
	public static final byte OP_LMUL = 105;
	public static final byte OP_FMUL = 106;
	public static final byte OP_DMUL = 107;
	public static final byte OP_IDIV = 108;
	public static final byte OP_LDIV = 109;
	public static final byte OP_FDIV = 110;
	public static final byte OP_DDIV = 111;
	public static final byte OP_IREM = 112;
	public static final byte OP_LREM = 113;
	public static final byte OP_FREM = 114;
	public static final byte OP_DREM = 115;
	public static final byte OP_INEG = 116;
	public static final byte OP_LNEG = 117;
	public static final byte OP_FNEG = 118;
	public static final byte OP_DNEG = 119;
	public static final byte OP_ISHL = 120;
	public static final byte OP_LSHL = 121;
	public static final byte OP_ISHR = 122;
	public static final byte OP_LSHR = 123;
	public static final byte OP_IUSHR = 124;
	public static final byte OP_LUSHR = 125;
	public static final byte OP_IAND = 126;
	public static final byte OP_LAND = 127;
	public static final byte OP_IOR = -128;
	public static final byte OP_LOR = -127;
	public static final byte OP_IXOR = -126;
	public static final byte OP_LXOR = -125;
	public static final byte OP_IINC = -124;
	public static final byte OP_I2L = -123;
	public static final byte OP_I2F = -122;
	public static final byte OP_I2D = -121;
	public static final byte OP_L2I = -120;
	public static final byte OP_L2F = -119;
	public static final byte OP_L2D = -118;
	public static final byte OP_F2I = -117;
	public static final byte OP_F2L = -116;
	public static final byte OP_F2D = -115;
	public static final byte OP_D2I = -114;
	public static final byte OP_D2L = -113;
	public static final byte OP_D2F = -112;
	public static final byte OP_I2B = -111;
	public static final byte OP_I2C = -110;
	public static final byte OP_I2S = -109;
	public static final byte OP_LCMP = -108;
	public static final byte OP_FCMPL = -107;
	public static final byte OP_FCMPG = -106;
	public static final byte OP_DCMPL = -105;
	public static final byte OP_DCMPG = -104;
	public static final byte OP_IFEQ = -103;
	public static final byte OP_IFNE = -102;
	public static final byte OP_IFLT = -101;
	public static final byte OP_IFGE = -100;
	public static final byte OP_IFGT = -99;
	public static final byte OP_IFLE = -98;
	public static final byte OP_IF_ICMPEQ = -97;
	public static final byte OP_IF_ICMPNE = -96;
	public static final byte OP_IF_ICMPLT = -95;
	public static final byte OP_IF_ICMPGE = -94;
	public static final byte OP_IF_ICMPGT = -93;
	public static final byte OP_IF_ICMPLE = -92;
	public static final byte OP_IF_ACMPEQ = -91;
	public static final byte OP_IF_ACMPNE = -90;
	public static final byte OP_GOTO = -89;
	public static final byte OP_JSR = -88;
	public static final byte OP_RET = -87;
	public static final byte OP_TABLESWITCH = -86;
	public static final byte OP_LOOKUPSWITCH = -85;
	public static final byte OP_IRETURN = -84;
	public static final byte OP_LRETURN = -83;
	public static final byte OP_FRETURN = -82;
	public static final byte OP_DRETURN = -81;
	public static final byte OP_ARETURN = -80;
	public static final byte OP_RETURN = -79;
	public static final byte OP_GETSTATIC = -78;
	public static final byte OP_PUTSTATIC = -77;
	public static final byte OP_GETFIELD = -76;
	public static final byte OP_PUTFIELD = -75;
	public static final byte OP_INVOKEVIRTUAL = -74;
	public static final byte OP_INVOKESPECIAL = -73;
	public static final byte OP_INVOKESTATIC = -72;
	public static final byte OP_INVOKEINTERFACE = -71;
	public static final byte OP_INVOKEDYNAMIC = -70;
	public static final byte OP_NEW = -69;
	public static final byte OP_NEWARRAY = -68;
	public static final byte OP_ANEWARRAY = -67;
	public static final byte OP_ARRAYLENGTH = -66;
	public static final byte OP_ATHROW = -65;
	public static final byte OP_CHECKCAST = -64;
	public static final byte OP_INSTANCEOF = -63;
	public static final byte OP_MONITORENTER = -62;
	public static final byte OP_MONITOREXIT = -61;
	public static final byte OP_WIDE = -60;
	public static final byte OP_MULTIANEWARRAY = -59;
	public static final byte OP_IFNULL = -58;
	public static final byte OP_IFNONNULL = -57;
	public static final byte OP_GOTO_W = -56;
	public static final byte OP_JSR_W = -55;
	public static final byte OP_BREAKPOINT = -54;

	//internally used by JBSE
	public static final byte OP_INCORRECT = -53;
	public static final byte OP_INVOKEHANDLE = -52;

	//implementation-dependent
	public static final byte OP_IMPDEP1 = -2;
	public static final byte OP_IMPDEP2 = -1;

	public static String opcodeName(byte opcode) {
		switch (opcode) {
		case OP_NOP: return "NOP";
		case OP_ACONST_NULL: return "ACONST_NULL";
		case OP_ICONST_M1: return "ICONST_M1";
		case OP_ICONST_0: return "ICONST_0";
		case OP_ICONST_1: return "ICONST_1";
		case OP_ICONST_2: return "ICONST_2";
		case OP_ICONST_3: return "ICONST_3";
		case OP_ICONST_4: return "ICONST_4";
		case OP_ICONST_5: return "ICONST_5";
		case OP_LCONST_0: return "LCONST_0";
		case OP_LCONST_1: return "LCONST_1";
		case OP_FCONST_0: return "FCONST_0";
		case OP_FCONST_1: return "FCONST_1";
		case OP_FCONST_2: return "FCONST_2";
		case OP_DCONST_0: return "DCONST_0";
		case OP_DCONST_1: return "DCONST_1";
		case OP_BIPUSH: return "BIPUSH";
		case OP_SIPUSH: return "SIPUSH";
		case OP_LDC: return "LDC";
		case OP_LDC_W: return "LDC_W";
		case OP_LDC2_W: return "LDC2_W";
		case OP_ILOAD: return "ILOAD";
		case OP_LLOAD: return "LLOAD";
		case OP_FLOAD: return "FLOAD";
		case OP_DLOAD: return "DLOAD";
		case OP_ALOAD: return "ALOAD";
		case OP_ILOAD_0: return "ILOAD_0";
		case OP_ILOAD_1: return "ILOAD_1";
		case OP_ILOAD_2: return "ILOAD_2";
		case OP_ILOAD_3: return "ILOAD_3";
		case OP_LLOAD_0: return "LLOAD_0";
		case OP_LLOAD_1: return "LLOAD_1";
		case OP_LLOAD_2: return "LLOAD_2";
		case OP_LLOAD_3: return "LLOAD_3";
		case OP_FLOAD_0: return "FLOAD_0";
		case OP_FLOAD_1: return "FLOAD_1";
		case OP_FLOAD_2: return "FLOAD_2";
		case OP_FLOAD_3: return "FLOAD_3";
		case OP_DLOAD_0: return "DLOAD_0";
		case OP_DLOAD_1: return "DLOAD_1";
		case OP_DLOAD_2: return "DLOAD_2";
		case OP_DLOAD_3: return "DLOAD_3";
		case OP_ALOAD_0: return "ALOAD_0";
		case OP_ALOAD_1: return "ALOAD_1";
		case OP_ALOAD_2: return "ALOAD_2";
		case OP_ALOAD_3: return "ALOAD_3";
		case OP_IALOAD: return "IALOAD";
		case OP_LALOAD: return "LALOAD";
		case OP_FALOAD: return "FALOAD";
		case OP_DALOAD: return "DALOAD";
		case OP_AALOAD: return "AALOAD";
		case OP_BALOAD: return "BALOAD";
		case OP_CALOAD: return "CALOAD";
		case OP_SALOAD: return "SALOAD";
		case OP_ISTORE: return "ISTORE";
		case OP_LSTORE: return "ISTORE";
		case OP_FSTORE: return "ISTORE";
		case OP_DSTORE: return "ISTORE";
		case OP_ASTORE: return "ISTORE";
		case OP_ISTORE_0: return "ISTORE_0";
		case OP_ISTORE_1: return "ISTORE_1";
		case OP_ISTORE_2: return "ISTORE_2";
		case OP_ISTORE_3: return "ISTORE_3";
		case OP_LSTORE_0: return "LSTORE_0";
		case OP_LSTORE_1: return "LSTORE_1";
		case OP_LSTORE_2: return "LSTORE_2";
		case OP_LSTORE_3: return "LSTORE_3";
		case OP_FSTORE_0: return "FSTORE_0";
		case OP_FSTORE_1: return "FSTORE_1";
		case OP_FSTORE_2: return "FSTORE_2";
		case OP_FSTORE_3: return "FSTORE_3";
		case OP_DSTORE_0: return "DSTORE_0";
		case OP_DSTORE_1: return "DSTORE_1";
		case OP_DSTORE_2: return "DSTORE_2";
		case OP_DSTORE_3: return "DSTORE_3";
		case OP_ASTORE_0: return "ASTORE_0";
		case OP_ASTORE_1: return "ASTORE_1";
		case OP_ASTORE_2: return "ASTORE_2";
		case OP_ASTORE_3: return "ASTORE_3";
		case OP_IASTORE: return "IASTORE";
		case OP_LASTORE: return "LASTORE";
		case OP_FASTORE: return "FASTORE";
		case OP_DASTORE: return "DASTORE";
		case OP_AASTORE: return "AASTORE";
		case OP_BASTORE: return "BASTORE";
		case OP_CASTORE: return "CASTORE";
		case OP_SASTORE: return "SASTORE";
		case OP_POP: return "POP";
		case OP_POP2: return "POP2";
		case OP_DUP: return "DUP";
		case OP_DUP_X1: return "DUP_X1";
		case OP_DUP_X2: return "DUP_X2";
		case OP_DUP2: return "DUP2";
		case OP_DUP2_X1: return "DUP2_X1";
		case OP_DUP2_X2: return "DUP2_X2";
		case OP_SWAP: return "SWAP";
		case OP_IADD: return "IADD";
		case OP_LADD: return "LADD";
		case OP_FADD: return "FADD";
		case OP_DADD: return "DADD";
		case OP_ISUB: return "ISUB";
		case OP_LSUB: return "LSUB";
		case OP_FSUB: return "FSUB";
		case OP_DSUB: return "DSUB";
		case OP_IMUL: return "IMUL";
		case OP_LMUL: return "LMUL";
		case OP_FMUL: return "FMUL";
		case OP_DMUL: return "DMUL";
		case OP_IDIV: return "IDIV";
		case OP_LDIV: return "LDIV";
		case OP_FDIV: return "FDIV";
		case OP_DDIV: return "DDIV";
		case OP_IREM: return "IREM";
		case OP_LREM: return "LREM";
		case OP_FREM: return "FREM";
		case OP_DREM: return "DREM";
		case OP_INEG: return "INEG";
		case OP_LNEG: return "LNEG";
		case OP_FNEG: return "FNEG";
		case OP_DNEG: return "DNEG";
		case OP_ISHL: return "ISHL";
		case OP_LSHL: return "LSHL";
		case OP_ISHR: return "ISHR";
		case OP_LSHR: return "LSHR";
		case OP_IUSHR: return "IUSHR";
		case OP_LUSHR: return "LUSHR";
		case OP_IAND: return "IAND";
		case OP_LAND: return "LAND";
		case OP_IOR: return "IOR";
		case OP_LOR: return "LOR";
		case OP_IXOR: return "IXOR";
		case OP_LXOR: return "LXOR";
		case OP_IINC: return "IINC";
		case OP_I2L: return "I2L";
		case OP_I2F: return "I2F";
		case OP_I2D: return "I2D";
		case OP_L2I: return "L2I";
		case OP_L2F: return "L2F";
		case OP_L2D: return "L2D";
		case OP_F2I: return "F2I";
		case OP_F2L: return "F2L";
		case OP_F2D: return "F2D";
		case OP_D2I: return "D2I";
		case OP_D2L: return "D2L";
		case OP_D2F: return "D2F";
		case OP_I2B: return "I2B";
		case OP_I2C: return "I2C";
		case OP_I2S: return "I2S";
		case OP_LCMP: return "LCMP";
		case OP_FCMPL: return "FCMPL";
		case OP_FCMPG: return "FCMPG";
		case OP_DCMPL: return "DCMPL";
		case OP_DCMPG: return "DCMPG";
		case OP_IFEQ: return "IFEQ";
		case OP_IFNE: return "IFNE";
		case OP_IFLT: return "IFLT";
		case OP_IFGE: return "IFGE";
		case OP_IFGT: return "IFGT";
		case OP_IFLE: return "IFLE";
		case OP_IF_ICMPEQ: return "IF_ICMPEQ";
		case OP_IF_ICMPNE: return "IF_ICMPNE";
		case OP_IF_ICMPLT: return "IF_ICMPLT";
		case OP_IF_ICMPGE: return "IF_ICMPGE";
		case OP_IF_ICMPGT: return "IF_ICMPGT";
		case OP_IF_ICMPLE: return "IF_ICMPLE";
		case OP_IF_ACMPEQ: return "IF_ACMPEQ";
		case OP_IF_ACMPNE: return "IF_ACMPNE";
		case OP_GOTO: return "GOTO";
		case OP_JSR: return "JSR";
		case OP_RET: return "RET";
		case OP_TABLESWITCH: return "TABLESWITCH";
		case OP_LOOKUPSWITCH: return "LOOKUPSWITCH";
		case OP_IRETURN: return "IRETURN";
		case OP_LRETURN: return "LRETURN";
		case OP_FRETURN: return "FRETURN";
		case OP_DRETURN: return "DRETURN";
		case OP_ARETURN: return "ARETURN";
		case OP_RETURN: return "RETURN";
		case OP_GETSTATIC: return "GETSTATIC";
		case OP_PUTSTATIC: return "PUTSTATIC";
		case OP_GETFIELD: return "GETFIELD";
		case OP_PUTFIELD: return "PUTFIELD";
		case OP_INVOKEVIRTUAL: return "INVOKEVIRTUAL";
		case OP_INVOKESPECIAL: return "INVOKESPECIAL";
		case OP_INVOKESTATIC: return "INVOKESTATIC";
		case OP_INVOKEINTERFACE: return "INVOKEINTERFACE";
		case OP_INVOKEDYNAMIC: return "INVOKEDYNAMIC";
		case OP_NEW: return "NEW";
		case OP_NEWARRAY: return "NEWARRAY";
		case OP_ANEWARRAY: return "ANEWARRAY";
		case OP_ARRAYLENGTH: return "ARRAYLENGTH";
		case OP_ATHROW: return "ATHROW";
		case OP_CHECKCAST: return "CHECKCAST";
		case OP_INSTANCEOF: return "INSTANCEOF";
		case OP_MONITORENTER: return "MONITORENTER";
		case OP_MONITOREXIT: return "MONITOREXIT";
		case OP_WIDE: return "WIDE";
		case OP_MULTIANEWARRAY: return "MULTIANEWARRAY";
		case OP_IFNULL: return "IFNULL";
		case OP_IFNONNULL: return "IFNONNULL";
		case OP_GOTO_W: return "GOTO_W";
		case OP_JSR_W: return "JSR_W";
		case OP_BREAKPOINT: return "BREAKPOINT";

		//internally used by JBSE
		case OP_INVOKEHANDLE: return "INVOKEHANDLE";

		//implementation-dependent
		case OP_IMPDEP1: return "IMPDEP1";
		case OP_IMPDEP2: return "IMPDEP2";

		default: return "<INCORRECT>";
		}
	}

	/**
	 * Checks whether a bytecode is a jump bytecode.
	 * 
	 * @param bytecode a {@code byte}.
	 * @return {@code true} iff {@code bytecode} is a jump bytecode.
	 */
	public static boolean isBytecodeJump(byte bytecode) {
		return (bytecode == OP_IF_ACMPEQ ||
		bytecode == OP_IF_ACMPNE ||	
		bytecode == OP_IFNONNULL ||	
		bytecode == OP_IFNULL ||	
		bytecode == OP_IFEQ ||
		bytecode == OP_IFGE ||	
		bytecode == OP_IFGT ||	
		bytecode == OP_IFLE ||	
		bytecode == OP_IFLT ||	
		bytecode == OP_IFNE ||	
		bytecode == OP_IF_ICMPEQ ||	
		bytecode == OP_IF_ICMPGE ||	
		bytecode == OP_IF_ICMPGT ||	
		bytecode == OP_IF_ICMPLE ||	
		bytecode == OP_IF_ICMPLT ||	
		bytecode == OP_IF_ICMPNE ||	
		bytecode == OP_LOOKUPSWITCH ||	
		bytecode == OP_TABLESWITCH);
	}

	/**
	 * Checks whether a bytecode is an invoke* bytecode.
	 * 
	 * @param bytecode a {@code byte}.
	 * @return {@code true} iff {@code bytecode} is an invoke*.
	 */
	public static boolean isBytecodeInvoke(byte bytecode) {
		return (bytecode == OP_INVOKEVIRTUAL ||
		bytecode == OP_INVOKESTATIC ||
		bytecode == OP_INVOKEINTERFACE ||
		bytecode == OP_INVOKESPECIAL ||
		bytecode == OP_INVOKEDYNAMIC ||
		bytecode == OP_INVOKEHANDLE);
	}

	/**
	 * Checks whether a bytecode is a load bytecode.
	 * 
	 * @param bytecode a {@code byte}.
	 * @return {@code true} iff {@code currentBytecode} is a load bytecode.
	 */
	public static boolean isBytecodeLoad(byte bytecode) {
		return (bytecode == OP_ALOAD || 
		bytecode == OP_ALOAD_0 || 
		bytecode == OP_ALOAD_1 || 
		bytecode == OP_ALOAD_2 || 
		bytecode == OP_ALOAD_3 || 
		bytecode == OP_IALOAD ||
		bytecode == OP_LALOAD ||
		bytecode == OP_FALOAD ||
		bytecode == OP_DALOAD ||
		bytecode == OP_AALOAD || 
		bytecode == OP_BALOAD ||
		bytecode == OP_CALOAD ||
		bytecode == OP_SALOAD ||
		bytecode == OP_GETSTATIC ||
		bytecode == OP_GETFIELD ||
		bytecode == OP_LDC ||
		bytecode == OP_LDC_W ||
		bytecode == OP_LDC2_W);
	}

	/**
	 * Checks whether a bytecode is branching.
	 * 
	 * @param bytecode a {@code byte}.
	 * @return {@code true} iff {@code bytecode} is branching.
	 */
	public static boolean isBytecodeBranch(byte bytecode) {
		return (isBytecodeJump(bytecode) ||
		isBytecodeInvoke(bytecode) ||
		bytecode == OP_ALOAD ||
		bytecode == OP_ALOAD_0 ||
		bytecode == OP_ALOAD_1 ||
		bytecode == OP_ALOAD_2 ||
		bytecode == OP_ALOAD_3 ||
		bytecode == OP_IALOAD ||
		bytecode == OP_LALOAD ||
		bytecode == OP_FALOAD ||
		bytecode == OP_DALOAD ||
		bytecode == OP_AALOAD ||
		bytecode == OP_BALOAD ||
		bytecode == OP_CALOAD ||
		bytecode == OP_SALOAD ||
		bytecode == OP_GETSTATIC ||
		bytecode == OP_GETFIELD ||
		bytecode == OP_IASTORE ||
		bytecode == OP_LASTORE ||
		bytecode == OP_FASTORE ||
		bytecode == OP_DASTORE ||
		bytecode == OP_AASTORE ||
		bytecode == OP_BASTORE ||
		bytecode == OP_CASTORE ||
		bytecode == OP_LCMP ||
		bytecode == OP_FCMPL ||
		bytecode == OP_FCMPG ||
		bytecode == OP_DCMPL ||
		bytecode == OP_DCMPG ||
		bytecode == OP_NEWARRAY ||
		bytecode == OP_ANEWARRAY ||
		bytecode == OP_MULTIANEWARRAY);
	}

	/**
	 * Checks whether a bytecode is a *return bytecode.
	 * 
	 * @param bytecode a {@code byte}.
	 * @return {@code true} iff {@code bytecode} is a *return.
	 */
	public static boolean isBytecodeReturn(byte bytecode) {
		return (bytecode == OP_IRETURN ||
		bytecode == OP_FRETURN ||
		bytecode == OP_DRETURN ||
		bytecode == OP_ARETURN ||
		bytecode == OP_RETURN);
	}

	/**
	 * Checks whether a bytecode may (not necessarily will)
	 * change the frame.
	 * 
	 * @param bytecode a {@code byte}.
	 * @return {@code true} iff {@code bytecode} is an invoke*,
	 *         a *return, or athrow. Note that only athrow might
	 *         not change frame (if the same frame catches the
	 *         exception).
	 */
	public static boolean isBytecodeFrameChanger(byte bytecode) {
		return (isBytecodeInvoke(bytecode) ||
		isBytecodeReturn(bytecode) ||
		bytecode == OP_ATHROW);
	}

	/**
	 * Do not instantiate it!
	 */
	private Opcodes() { 
		throw new AssertionError();
	}
}
