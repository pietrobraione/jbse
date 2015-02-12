package jbse.apps;

import static jbse.bc.Opcodes.*;

import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Dispatcher;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Frame;
import jbse.mem.SwitchTable;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A disassembler for the current bytecode of a frame.
 * 
 * @author Pietro Braione
 *
 */
class DispatcherBytecodeFormatter extends Dispatcher<Byte,DispatcherBytecodeFormatter.FormatterStrategy> {
	private final static String UNRECOGNIZED_BYTECODE = "<???>";
	private final static String INCORRECT_BYTECODE    = "<INCORRECT>";
    private final static String RESERVED_BYTECODE     = "<RESERVED>";
	
	interface FormatterStrategy {
		String format(Frame f, ClassHierarchy hier);
	}
	
	/**
	 * A formatter for bytecodes with 0 operands, which just returns their name.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat0 implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat0(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				@Override
				public String format(Frame f, ClassHierarchy hier) { 
					return DispatchStrategyFormat0.this.text; 
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 0 operands, which returns their name plus a local
	 * variable whose index is specified in the constructor.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat0LV implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		private final int slot;
		public DispatchStrategyFormat0LV(String text, int slot) { this.text = text; this.slot = slot; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				@Override
				public String format(Frame f, ClassHierarchy hier) { 
					final String varName = f.getLocalVariableName(DispatchStrategyFormat0LV.this.slot);
					return DispatchStrategyFormat0LV.this.text + (varName == null ? "" : " [" + varName + "]"); 
				}
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) whose
	 * meaning is a primitive type in newarray coding.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1AT implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1AT(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1AT.this.text + " ";
					try {
						final short UB = f.getInstruction(1);
						final String type;
						switch (Array.checkAndReturnArrayPrimitive(UB)) {
						case Type.BOOLEAN:
							type = "bool";
							break;
						case Type.CHAR:
							type = "char";
							break;
						case Type.FLOAT:
							type = "float";
							break;
						case Type.DOUBLE:
							type = "double";
							break;
						case Type.BYTE:
							type = "byte";
							break;
						case Type.SHORT:
							type = "short";
							break;
						case Type.INT:
							type = "int";
							break;
						case Type.LONG:
							type = "long";
							break;
						default:
						    type = null;
							break;
						}
						if (type == null) {
							retVal += UNRECOGNIZED_BYTECODE;
						} else {
							retVal += type + " [" + UB + "]";
						}
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal = UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type signed byte (8 bits).
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1SB implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1SB(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1SB.this.text + " ";
					try {
						final byte SB = f.getInstruction(1);
						retVal += SB;
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type signed word (16 bits).
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1SW implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1SW(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1SW.this.text + " ";
					try {
						final short SW = Util.byteCatShort(f.getInstruction(1), f.getInstruction(2));
						retVal += SW;
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	/**
	 * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) or unsigned 
	 * word (16 bits) whose meaning is the index of a local variable.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1LV implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1LV(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1LV.this.text + " ";
					try {
						//determines whether the operand is wide
						boolean wide = false;
						try {
							final byte prev = f.getInstruction(-1);
							wide = (prev == OP_WIDE);
						} catch (InvalidProgramCounterException e) {
							//do nothing (not a wide && first bytecode in function)
						}
						final int UW;
						if (wide) {
							UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
						} else {
							UW = f.getInstruction(1);
						}
						final String varName = f.getLocalVariableName(UW);
						retVal += (varName == null ? UW : varName + " [" + UW + "]");
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type signed word (16 bits) whose meaning 
	 * is a (near) jump offset.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1ON implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1ON(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1ON.this.text + " ";
					try {
						final short SW = Util.byteCatShort(f.getInstruction(1), f.getInstruction(2));
						final int target = f.getPC() + SW;
						retVal += target;
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type signed dword (32 bits) whose meaning 
	 * is a (far) jump offset.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1OF implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1OF(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1OF.this.text + " ";
					try {
						final int SD = Util.byteCat(f.getInstruction(1), f.getInstruction(2), f.getInstruction(3), f.getInstruction(4));
						final int target = f.getPC() + SD;
						retVal += target;
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
	 * meaning is a literal constant in the constant pool.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1CO implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		private final boolean wide;
		public DispatchStrategyFormat1CO(String text, boolean wide) { this.text = text; this.wide = wide; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1CO.this.text + " ";
					try {
						final int UW;
						if (wide) {
							UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
						} else {
							UW = f.getInstruction(1);
						}
						final ConstantPoolValue cpVal = 
						    hier.getClassFile(f.getCurrentMethodSignature().getClassName()).getValueFromConstantPool(UW);
						final Object val = cpVal.getValue();
						
						if (val instanceof Integer) {
							retVal += "<int " + cpVal + ">";
						} else if (val instanceof Long) {
							retVal += "<long " + cpVal + ">";
						} else if (val instanceof Float) {
							retVal += "<float " + cpVal + ">";
						} else if (val instanceof Double) {
							retVal += "<double " + cpVal + ">";
						} else if (val instanceof String && cpVal instanceof ConstantPoolString) {
							retVal += "<String \"" + cpVal + "\">";
                        } else if (val instanceof String && cpVal instanceof ConstantPoolClass) {
                            retVal += "<Class " + cpVal + ">";
						} else {
							retVal += UNRECOGNIZED_BYTECODE;
						}
					} catch (InvalidProgramCounterException | InvalidIndexException |
							BadClassFileException | UnexpectedInternalException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}

					return retVal;
					
				} 
			};
		}		
	}
	
	/**
	 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
	 * meaning is a class/array/interface signature in the constant pool.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1CL implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1CL(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1CL.this.text + " ";
					try {
						final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
						final String sig = hier.getClassFile(f.getCurrentMethodSignature().getClassName()).getClassSignature(UW);
						retVal += sig;
					} catch (InvalidProgramCounterException | InvalidIndexException |
							BadClassFileException | UnexpectedInternalException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	/**
	 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
	 * meaning is a field signature in the constant pool.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1FI implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat1FI(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1FI.this.text + " ";
					try {
						final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
						final Signature sig = hier.getClassFile(f.getCurrentMethodSignature().getClassName()).getFieldSignature(UW);
						retVal += sig.getClassName() + Signature.SIGNATURE_SEPARATOR + sig.getName() + " [" + UW + "]";
					} catch (InvalidProgramCounterException | InvalidIndexException |
					         BadClassFileException | UnexpectedInternalException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	/**
	 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
	 * meaning is a method signature in the constant pool.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat1ME implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		private final boolean isInterface;
		public DispatchStrategyFormat1ME(String text, boolean isInterface) { this.text = text; this.isInterface = isInterface;}
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat1ME.this.text + " ";
					try {
					    final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
						final Signature sig;
						if (isInterface) {
							sig = hier.getClassFile(f.getCurrentMethodSignature().getClassName()).getInterfaceMethodSignature(UW);
						} else {
							sig = hier.getClassFile(f.getCurrentMethodSignature().getClassName()).getMethodSignature(UW);
						}
						retVal += sig + " [" + UW + "]";
					} catch (InvalidProgramCounterException | InvalidIndexException |
					         BadClassFileException | UnexpectedInternalException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	/**
	 * A formatter for bytecodes with 2 operands, the first with type unsigned word (16 bits) 
	 * whose meaning is a class/array/interface signature in the constant pool, the second
	 * with type unsigned byte.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat2CLUB implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat2CLUB(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat2CLUB.this.text + " ";
					try {
						final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
						final short UB = f.getInstruction(3);
						final String sig = hier.getClassFile(f.getCurrentMethodSignature().getClassName()).getClassSignature(UW);
						retVal += sig + " " + UB + " [" + UW + "]";
					} catch (InvalidProgramCounterException | InvalidIndexException |
					         BadClassFileException | UnexpectedInternalException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	/**
	 * A formatter for bytecodes with 2 operand, the first with type unsigned byte (8 bits) or 
	 * unsigned word (16 bits) whose meaning is the index of a local variable, the second
	 * with type signed byte (8 bits) or signed word (16 bits) whose meaning is an immediate
	 * constant.
	 * 
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormat2LVIM implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		public DispatchStrategyFormat2LVIM(String text) { this.text = text; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormat2LVIM.this.text + " ";
					try {
						//determines whether the operand is wide
						boolean wide = false;
						try {
							final byte prev = f.getInstruction(-1);
							wide = (prev == OP_WIDE);
						} catch (InvalidProgramCounterException e) {
							//do nothing (not a wide && first bytecode in function)
						}
						int UW0, UW1;
						if (wide) {
							UW0 = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
							UW1 = Util.byteCat(f.getInstruction(3), f.getInstruction(4));
						} else {
							UW0 = f.getInstruction(1);
							UW1 = f.getInstruction(2);
						}
						final String varName = f.getLocalVariableName(UW0);
						retVal += UW0 + " " + UW1 + (varName == null ? "" : " [" + varName + "]");
					} catch (InvalidProgramCounterException e) {
						//unrecognized bytecode
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	/**
	 * A formatter for *switch bytecodes.
	 *
	 * @author Pietro Braione
	 */
	private static class DispatchStrategyFormatSWITCH implements Dispatcher.DispatchStrategy<FormatterStrategy> {
		private final String text;
		private final boolean isTableSwitch;
		public DispatchStrategyFormatSWITCH(String text, boolean isTableSwitch) { this.text = text; this.isTableSwitch = isTableSwitch; }
		public FormatterStrategy doIt() {
			return new FormatterStrategy() {
				public String format(Frame f, ClassHierarchy hier) { 
					String retVal = DispatchStrategyFormatSWITCH.this.text + " ";
					SwitchTable tab;
					try {
						tab = new SwitchTable(f, null, DispatchStrategyFormatSWITCH.this.isTableSwitch);
						final StringBuilder buf = new StringBuilder();
						for (int val : tab) {
							int target = f.getPC() + tab.jumpOffset(val);
							buf.append(val);
							buf.append(":");
							buf.append(target);
							buf.append(" ");
							//retVal += val + ":" + target + " ";
						}
						retVal += buf.toString();
						int target = f.getPC() + tab.jumpOffsetDefault();
						retVal += "dflt:" + target;
					} catch (InvalidProgramCounterException e) {
						retVal += UNRECOGNIZED_BYTECODE;
					}
					return retVal;
					
				} 
			};
		}		
	}

	DispatcherBytecodeFormatter() {
		this
			.setDispatchNonexistentStrategy(new DispatchStrategyFormat0(INCORRECT_BYTECODE))
			.setDispatchStrategy(OP_NOP,             new DispatchStrategyFormat0("NOP"))
			.setDispatchStrategy(OP_ALOAD,           new DispatchStrategyFormat1LV("ALOAD"))
			.setDispatchStrategy(OP_DLOAD,           new DispatchStrategyFormat1LV("DLOAD"))
			.setDispatchStrategy(OP_FLOAD,           new DispatchStrategyFormat1LV("FLOAD"))
			.setDispatchStrategy(OP_ILOAD,           new DispatchStrategyFormat1LV("ILOAD"))
			.setDispatchStrategy(OP_LLOAD,           new DispatchStrategyFormat1LV("LLOAD"))
			.setDispatchStrategy(OP_ALOAD_0,         new DispatchStrategyFormat0LV("ALOAD_0", 0))
			.setDispatchStrategy(OP_DLOAD_0,         new DispatchStrategyFormat0LV("DLOAD_0", 0))
			.setDispatchStrategy(OP_FLOAD_0,         new DispatchStrategyFormat0LV("FLOAD_0", 0))
			.setDispatchStrategy(OP_ILOAD_0,         new DispatchStrategyFormat0LV("ILOAD_0", 0))
			.setDispatchStrategy(OP_LLOAD_0,         new DispatchStrategyFormat0LV("LLOAD_0", 0))
			.setDispatchStrategy(OP_ALOAD_1,         new DispatchStrategyFormat0LV("ALOAD_1", 1))
			.setDispatchStrategy(OP_DLOAD_1,         new DispatchStrategyFormat0LV("DLOAD_1", 1))
			.setDispatchStrategy(OP_FLOAD_1,         new DispatchStrategyFormat0LV("FLOAD_1", 1))
			.setDispatchStrategy(OP_ILOAD_1,         new DispatchStrategyFormat0LV("ILOAD_1", 1))
			.setDispatchStrategy(OP_LLOAD_1,         new DispatchStrategyFormat0LV("LLOAD_1", 1))
			.setDispatchStrategy(OP_ALOAD_2,         new DispatchStrategyFormat0LV("ALOAD_2", 2))
			.setDispatchStrategy(OP_DLOAD_2,         new DispatchStrategyFormat0LV("DLOAD_2", 2))
			.setDispatchStrategy(OP_FLOAD_2,         new DispatchStrategyFormat0LV("FLOAD_2", 2))
			.setDispatchStrategy(OP_ILOAD_2,         new DispatchStrategyFormat0LV("ILOAD_2", 2))
			.setDispatchStrategy(OP_LLOAD_2,         new DispatchStrategyFormat0LV("LLOAD_2", 2))
			.setDispatchStrategy(OP_ALOAD_3,         new DispatchStrategyFormat0LV("ALOAD_3", 3))
			.setDispatchStrategy(OP_DLOAD_3,         new DispatchStrategyFormat0LV("DLOAD_3", 3))
			.setDispatchStrategy(OP_FLOAD_3,         new DispatchStrategyFormat0LV("FLOAD_3", 3))
			.setDispatchStrategy(OP_ILOAD_3,         new DispatchStrategyFormat0LV("ILOAD_3", 3))
			.setDispatchStrategy(OP_LLOAD_3,         new DispatchStrategyFormat0LV("LLOAD_3", 3))
			.setDispatchStrategy(OP_ASTORE,          new DispatchStrategyFormat1LV("ASTORE"))
			.setDispatchStrategy(OP_DSTORE,          new DispatchStrategyFormat1LV("DSTORE"))
			.setDispatchStrategy(OP_FSTORE,          new DispatchStrategyFormat1LV("FSTORE"))
			.setDispatchStrategy(OP_ISTORE,          new DispatchStrategyFormat1LV("ISTORE"))
			.setDispatchStrategy(OP_LSTORE,          new DispatchStrategyFormat1LV("LSTORE"))
			.setDispatchStrategy(OP_ASTORE_0,        new DispatchStrategyFormat0LV("ASTORE_0", 0))
			.setDispatchStrategy(OP_DSTORE_0,        new DispatchStrategyFormat0LV("DSTORE_0", 0))
			.setDispatchStrategy(OP_FSTORE_0,        new DispatchStrategyFormat0LV("FSTORE_0", 0))
			.setDispatchStrategy(OP_ISTORE_0,        new DispatchStrategyFormat0LV("ISTORE_0", 0))
			.setDispatchStrategy(OP_LSTORE_0,        new DispatchStrategyFormat0LV("LSTORE_0", 0))
			.setDispatchStrategy(OP_ASTORE_1,        new DispatchStrategyFormat0LV("ASTORE_1", 1))
			.setDispatchStrategy(OP_DSTORE_1,        new DispatchStrategyFormat0LV("DSTORE_1", 1))
			.setDispatchStrategy(OP_FSTORE_1,        new DispatchStrategyFormat0LV("FSTORE_1", 1))
			.setDispatchStrategy(OP_ISTORE_1,        new DispatchStrategyFormat0LV("ISTORE_1", 1))
			.setDispatchStrategy(OP_LSTORE_1,        new DispatchStrategyFormat0LV("LSTORE_1", 1))
			.setDispatchStrategy(OP_ASTORE_2,        new DispatchStrategyFormat0LV("ASTORE_2", 2))
			.setDispatchStrategy(OP_DSTORE_2,        new DispatchStrategyFormat0LV("DSTORE_2", 2))
			.setDispatchStrategy(OP_FSTORE_2,        new DispatchStrategyFormat0LV("FSTORE_2", 2))
			.setDispatchStrategy(OP_ISTORE_2,        new DispatchStrategyFormat0LV("ISTORE_2", 2))
			.setDispatchStrategy(OP_LSTORE_2,        new DispatchStrategyFormat0LV("LSTORE_2", 2))
			.setDispatchStrategy(OP_ASTORE_3,        new DispatchStrategyFormat0LV("ASTORE_3", 3))
			.setDispatchStrategy(OP_DSTORE_3,        new DispatchStrategyFormat0LV("DSTORE_3", 3))
			.setDispatchStrategy(OP_FSTORE_3,        new DispatchStrategyFormat0LV("FSTORE_3", 3))
			.setDispatchStrategy(OP_ISTORE_3,        new DispatchStrategyFormat0LV("ISTORE_3", 3))
			.setDispatchStrategy(OP_LSTORE_3,        new DispatchStrategyFormat0LV("LSTORE_3", 3))
			.setDispatchStrategy(OP_BIPUSH,          new DispatchStrategyFormat1SB("BIPUSH"))
			.setDispatchStrategy(OP_SIPUSH,          new DispatchStrategyFormat1SW("SIPUSH"))
			.setDispatchStrategy(OP_LDC,             new DispatchStrategyFormat1CO("LDC", false))
			.setDispatchStrategy(OP_LDC_W,           new DispatchStrategyFormat1CO("LDC_W", true))
			.setDispatchStrategy(OP_LDC2_W,          new DispatchStrategyFormat1CO("LDC2_W", true))
			.setDispatchStrategy(OP_ACONST_NULL,     new DispatchStrategyFormat0("ACONST_NULL"))
			.setDispatchStrategy(OP_DCONST_0,        new DispatchStrategyFormat0("DCONST_0"))
			.setDispatchStrategy(OP_DCONST_1,        new DispatchStrategyFormat0("DCONST_1"))
			.setDispatchStrategy(OP_FCONST_0,        new DispatchStrategyFormat0("FCONST_0"))
			.setDispatchStrategy(OP_FCONST_1,        new DispatchStrategyFormat0("FCONST_1"))
			.setDispatchStrategy(OP_FCONST_2,        new DispatchStrategyFormat0("FCONST_2"))
			.setDispatchStrategy(OP_ICONST_M1,       new DispatchStrategyFormat0("ICONST_M1"))
			.setDispatchStrategy(OP_ICONST_0,        new DispatchStrategyFormat0("ICONST_0"))
			.setDispatchStrategy(OP_ICONST_1,        new DispatchStrategyFormat0("ICONST_1"))
			.setDispatchStrategy(OP_ICONST_2,        new DispatchStrategyFormat0("ICONST_2"))
			.setDispatchStrategy(OP_ICONST_3,        new DispatchStrategyFormat0("ICONST_3"))
			.setDispatchStrategy(OP_ICONST_4,        new DispatchStrategyFormat0("ICONST_4"))
			.setDispatchStrategy(OP_ICONST_5,        new DispatchStrategyFormat0("ICONST_5"))
			.setDispatchStrategy(OP_LCONST_0,        new DispatchStrategyFormat0("LCONST_0"))
			.setDispatchStrategy(OP_LCONST_1,        new DispatchStrategyFormat0("LCONST_1"))
			.setDispatchStrategy(OP_DADD,            new DispatchStrategyFormat0("DADD"))
			.setDispatchStrategy(OP_FADD,            new DispatchStrategyFormat0("FADD"))
			.setDispatchStrategy(OP_IADD,            new DispatchStrategyFormat0("IADD"))
			.setDispatchStrategy(OP_LADD,            new DispatchStrategyFormat0("LADD"))
			.setDispatchStrategy(OP_DSUB,            new DispatchStrategyFormat0("DSUB"))
			.setDispatchStrategy(OP_FSUB,            new DispatchStrategyFormat0("FSUB"))
			.setDispatchStrategy(OP_ISUB,            new DispatchStrategyFormat0("ISUB"))
			.setDispatchStrategy(OP_LSUB,            new DispatchStrategyFormat0("LSUB"))
			.setDispatchStrategy(OP_DMUL,            new DispatchStrategyFormat0("DMUL"))
			.setDispatchStrategy(OP_FMUL,            new DispatchStrategyFormat0("FMUL"))
			.setDispatchStrategy(OP_IMUL,            new DispatchStrategyFormat0("IMUL"))
			.setDispatchStrategy(OP_LMUL,            new DispatchStrategyFormat0("LMUL"))
			.setDispatchStrategy(OP_DDIV,            new DispatchStrategyFormat0("DDIV"))
			.setDispatchStrategy(OP_FDIV,            new DispatchStrategyFormat0("FDIV"))
			.setDispatchStrategy(OP_IDIV,            new DispatchStrategyFormat0("IDIV"))
			.setDispatchStrategy(OP_LDIV,            new DispatchStrategyFormat0("LDIV"))
			.setDispatchStrategy(OP_DREM,            new DispatchStrategyFormat0("DREM"))
			.setDispatchStrategy(OP_FREM,            new DispatchStrategyFormat0("FREM"))
			.setDispatchStrategy(OP_IREM,            new DispatchStrategyFormat0("IREM"))
			.setDispatchStrategy(OP_LREM,            new DispatchStrategyFormat0("LREM"))
			.setDispatchStrategy(OP_DNEG,            new DispatchStrategyFormat0("DNEG"))
			.setDispatchStrategy(OP_FNEG,            new DispatchStrategyFormat0("FNEG"))
			.setDispatchStrategy(OP_INEG,            new DispatchStrategyFormat0("INEG"))
			.setDispatchStrategy(OP_LNEG,            new DispatchStrategyFormat0("LNEG"))
			.setDispatchStrategy(OP_ISHL,            new DispatchStrategyFormat0("ISHL"))
			.setDispatchStrategy(OP_LSHL,            new DispatchStrategyFormat0("LSHL"))
			.setDispatchStrategy(OP_ISHR,            new DispatchStrategyFormat0("ISHR"))
			.setDispatchStrategy(OP_LSHR,            new DispatchStrategyFormat0("LSHR"))
			.setDispatchStrategy(OP_IUSHR,           new DispatchStrategyFormat0("IUSHL"))
			.setDispatchStrategy(OP_LUSHR,           new DispatchStrategyFormat0("LUSHL"))
			.setDispatchStrategy(OP_IOR,             new DispatchStrategyFormat0("IOR"))
			.setDispatchStrategy(OP_LOR,             new DispatchStrategyFormat0("LOR"))
			.setDispatchStrategy(OP_IAND,            new DispatchStrategyFormat0("IAND"))
			.setDispatchStrategy(OP_LAND,            new DispatchStrategyFormat0("LAND"))
			.setDispatchStrategy(OP_IXOR,            new DispatchStrategyFormat0("IXOR"))
			.setDispatchStrategy(OP_LXOR,            new DispatchStrategyFormat0("LXOR"))
			.setDispatchStrategy(OP_IINC,            new DispatchStrategyFormat2LVIM("IINC"))
			.setDispatchStrategy(OP_DCMPG,           new DispatchStrategyFormat0("DCMPG"))
			.setDispatchStrategy(OP_DCMPL,           new DispatchStrategyFormat0("DCMPL"))
			.setDispatchStrategy(OP_FCMPG,           new DispatchStrategyFormat0("FCMPG"))
			.setDispatchStrategy(OP_FCMPL,           new DispatchStrategyFormat0("FCMPL"))
			.setDispatchStrategy(OP_LCMP,            new DispatchStrategyFormat0("LCMP"))
			.setDispatchStrategy(OP_I2D,             new DispatchStrategyFormat0("I2D"))
			.setDispatchStrategy(OP_I2F,             new DispatchStrategyFormat0("I2F"))
			.setDispatchStrategy(OP_I2L,             new DispatchStrategyFormat0("I2L"))
			.setDispatchStrategy(OP_L2D,             new DispatchStrategyFormat0("L2D"))
			.setDispatchStrategy(OP_L2F,             new DispatchStrategyFormat0("L2F"))
			.setDispatchStrategy(OP_F2D,             new DispatchStrategyFormat0("F2D"))
			.setDispatchStrategy(OP_D2F,             new DispatchStrategyFormat0("D2F"))
			.setDispatchStrategy(OP_D2I,             new DispatchStrategyFormat0("D2I"))
			.setDispatchStrategy(OP_D2L,             new DispatchStrategyFormat0("D2L"))
			.setDispatchStrategy(OP_F2I,             new DispatchStrategyFormat0("F2I"))
			.setDispatchStrategy(OP_F2L,             new DispatchStrategyFormat0("F2L"))
			.setDispatchStrategy(OP_I2B,             new DispatchStrategyFormat0("I2B"))
			.setDispatchStrategy(OP_I2C,             new DispatchStrategyFormat0("I2C"))
			.setDispatchStrategy(OP_I2S,             new DispatchStrategyFormat0("I2S"))
			.setDispatchStrategy(OP_L2I,             new DispatchStrategyFormat0("L2I"))
			.setDispatchStrategy(OP_NEW,             new DispatchStrategyFormat1CL("NEW"))
			.setDispatchStrategy(OP_NEWARRAY,        new DispatchStrategyFormat1AT("NEWARRAY"))
			.setDispatchStrategy(OP_ANEWARRAY,       new DispatchStrategyFormat1CL("ANEWARRAY"))
			.setDispatchStrategy(OP_MULTIANEWARRAY,  new DispatchStrategyFormat2CLUB("MULTIANEWARRAY"))
			.setDispatchStrategy(OP_GETFIELD,        new DispatchStrategyFormat1FI("GETFIELD"))
			.setDispatchStrategy(OP_PUTFIELD,        new DispatchStrategyFormat1FI("PUTFIELD"))
			.setDispatchStrategy(OP_GETSTATIC,       new DispatchStrategyFormat1FI("GETSTATIC"))
			.setDispatchStrategy(OP_PUTSTATIC,       new DispatchStrategyFormat1FI("PUTSTATIC"))
			.setDispatchStrategy(OP_AALOAD,          new DispatchStrategyFormat0("AALOAD"))
			.setDispatchStrategy(OP_CALOAD,          new DispatchStrategyFormat0("CALOAD"))
			.setDispatchStrategy(OP_BALOAD,          new DispatchStrategyFormat0("BALOAD"))
			.setDispatchStrategy(OP_DALOAD,          new DispatchStrategyFormat0("DALOAD"))
			.setDispatchStrategy(OP_FALOAD,          new DispatchStrategyFormat0("FALOAD"))
			.setDispatchStrategy(OP_IALOAD,          new DispatchStrategyFormat0("IALOAD"))
			.setDispatchStrategy(OP_LALOAD,          new DispatchStrategyFormat0("LALOAD"))
			.setDispatchStrategy(OP_SALOAD,          new DispatchStrategyFormat0("SALOAD"))
			.setDispatchStrategy(OP_AASTORE,         new DispatchStrategyFormat0("AASTORE"))
			.setDispatchStrategy(OP_CASTORE,         new DispatchStrategyFormat0("CASTORE"))
			.setDispatchStrategy(OP_BASTORE,         new DispatchStrategyFormat0("BASTORE"))
			.setDispatchStrategy(OP_DASTORE,         new DispatchStrategyFormat0("DASTORE"))
			.setDispatchStrategy(OP_FASTORE,         new DispatchStrategyFormat0("FASTORE"))
			.setDispatchStrategy(OP_IASTORE,         new DispatchStrategyFormat0("IASTORE"))
			.setDispatchStrategy(OP_LASTORE,         new DispatchStrategyFormat0("LASTORE"))
			.setDispatchStrategy(OP_SASTORE,         new DispatchStrategyFormat0("SASTORE"))
			.setDispatchStrategy(OP_ARRAYLENGTH,     new DispatchStrategyFormat0("ARRAYLENGTH"))
			.setDispatchStrategy(OP_INSTANCEOF,      new DispatchStrategyFormat1CL("INSTANCEOF"))
			.setDispatchStrategy(OP_CHECKCAST,       new DispatchStrategyFormat1CL("CHECKCAST"))
			.setDispatchStrategy(OP_POP,             new DispatchStrategyFormat0("POP"))
			.setDispatchStrategy(OP_POP2,            new DispatchStrategyFormat0("POP2"))
			.setDispatchStrategy(OP_DUP,             new DispatchStrategyFormat0("DUP"))
			.setDispatchStrategy(OP_DUP2,            new DispatchStrategyFormat0("DUP2"))
			.setDispatchStrategy(OP_DUP_X1,          new DispatchStrategyFormat0("DUP_X1"))
			.setDispatchStrategy(OP_DUP2_X1,         new DispatchStrategyFormat0("DUP2_X1"))
			.setDispatchStrategy(OP_DUP_X2,          new DispatchStrategyFormat0("DUP_X2"))
			.setDispatchStrategy(OP_DUP2_X2,         new DispatchStrategyFormat0("DUP2_X2"))
			.setDispatchStrategy(OP_SWAP,            new DispatchStrategyFormat0("SWAP"))
			.setDispatchStrategy(OP_IFEQ,            new DispatchStrategyFormat1ON("IFEQ"))
			.setDispatchStrategy(OP_IFGE,            new DispatchStrategyFormat1ON("IFGE"))
			.setDispatchStrategy(OP_IFGT,            new DispatchStrategyFormat1ON("IFGT"))
			.setDispatchStrategy(OP_IFLE,            new DispatchStrategyFormat1ON("IFLE"))
			.setDispatchStrategy(OP_IFLT,            new DispatchStrategyFormat1ON("IFLT"))
			.setDispatchStrategy(OP_IFNE,            new DispatchStrategyFormat1ON("IFNE"))
			.setDispatchStrategy(OP_IFNONNULL,       new DispatchStrategyFormat1ON("IFNONNULL"))
			.setDispatchStrategy(OP_IFNULL,          new DispatchStrategyFormat1ON("IFNULL"))
			.setDispatchStrategy(OP_IF_ICMPEQ,       new DispatchStrategyFormat1ON("IF_ICMPEQ"))
			.setDispatchStrategy(OP_IF_ICMPGE,       new DispatchStrategyFormat1ON("IF_ICMPGE"))
			.setDispatchStrategy(OP_IF_ICMPGT,       new DispatchStrategyFormat1ON("IF_ICMPGT"))
			.setDispatchStrategy(OP_IF_ICMPLE,       new DispatchStrategyFormat1ON("IF_ICMPLE"))
			.setDispatchStrategy(OP_IF_ICMPLT,       new DispatchStrategyFormat1ON("IF_ICMPLT"))
			.setDispatchStrategy(OP_IF_ICMPNE,       new DispatchStrategyFormat1ON("IF_ICMPNE"))
			.setDispatchStrategy(OP_IF_ACMPEQ,       new DispatchStrategyFormat1ON("IF_ACMPEQ"))
			.setDispatchStrategy(OP_IF_ACMPNE,       new DispatchStrategyFormat1ON("IF_ACMPNE"))
			.setDispatchStrategy(OP_TABLESWITCH,     new DispatchStrategyFormatSWITCH("TABLESWITCH", true))
			.setDispatchStrategy(OP_LOOKUPSWITCH,    new DispatchStrategyFormatSWITCH("LOOKUPSWITCH", false))
			.setDispatchStrategy(OP_GOTO,            new DispatchStrategyFormat1ON("GOTO"))
			.setDispatchStrategy(OP_GOTO_W,          new DispatchStrategyFormat1OF("GOTO_W"))
			.setDispatchStrategy(OP_JSR,             new DispatchStrategyFormat1ON("JSR"))
			.setDispatchStrategy(OP_JSR_W,           new DispatchStrategyFormat1OF("JSR_W"))
			.setDispatchStrategy(OP_RET,             new DispatchStrategyFormat1LV("RET"))
			.setDispatchStrategy(OP_INVOKEINTERFACE, new DispatchStrategyFormat1ME("INVOKEINTERFACE", true))
			.setDispatchStrategy(OP_INVOKEVIRTUAL,   new DispatchStrategyFormat1ME("INVOKEVIRTUAL", false))
			.setDispatchStrategy(OP_INVOKESPECIAL,   new DispatchStrategyFormat1ME("INVOKESPECIAL", false))
			.setDispatchStrategy(OP_INVOKESTATIC,    new DispatchStrategyFormat1ME("INVOKESTATIC", false))
			.setDispatchStrategy(OP_RETURN,          new DispatchStrategyFormat0("RETURN"))
			.setDispatchStrategy(OP_ARETURN,         new DispatchStrategyFormat0("ARETURN"))
			.setDispatchStrategy(OP_DRETURN,         new DispatchStrategyFormat0("DRETURN"))
			.setDispatchStrategy(OP_FRETURN,         new DispatchStrategyFormat0("FRETURN"))
			.setDispatchStrategy(OP_IRETURN,         new DispatchStrategyFormat0("IRETURN"))
			.setDispatchStrategy(OP_LRETURN,         new DispatchStrategyFormat0("LRETURN"))
			.setDispatchStrategy(OP_ATHROW,          new DispatchStrategyFormat0("ATHROW"))
			.setDispatchStrategy(OP_WIDE,            new DispatchStrategyFormat0("WIDE"))
			.setDispatchStrategy(OP_MONITORENTER,    new DispatchStrategyFormat0("MONITORENTER"))
			.setDispatchStrategy(OP_MONITOREXIT,     new DispatchStrategyFormat0("MONITOREXIT"))
			.setDispatchStrategy(OP_BREAKPOINT,      new DispatchStrategyFormat0("BREAKPOINT"))
			.setDispatchStrategy(OP_IMPDEP1,         new DispatchStrategyFormat0("IMPDEP1"))
			.setDispatchStrategy(OP_IMPDEP2,         new DispatchStrategyFormat0("IMPDEP2"))
		;
		
		DispatchStrategyFormat0 s = new DispatchStrategyFormat0(RESERVED_BYTECODE);
		this
			.setDispatchStrategy(OP_INVOKEDYNAMIC,             s)
			.setDispatchStrategy(OP_ANEWARRAY_QUICK,           s)
			.setDispatchStrategy(OP_CHECKCAST_QUICK,           s)
			.setDispatchStrategy(OP_GETSTATIC_QUICK,           s)
			.setDispatchStrategy(OP_GETSTATIC2_QUICK,          s)
			.setDispatchStrategy(OP_GETFIELD_QUICK,            s)
			.setDispatchStrategy(OP_GETFIELD_QUICK_W,          s)
			.setDispatchStrategy(OP_GETFIELD2_QUICK,           s)
			.setDispatchStrategy(OP_INSTANCEOF_QUICK,          s)
			.setDispatchStrategy(OP_INVOKEINTERFACE_QUICK,     s)
			.setDispatchStrategy(OP_INVOKENONVIRTUAL_QUICK,    s)
			.setDispatchStrategy(OP_INVOKESTATIC_QUICK,        s)
			.setDispatchStrategy(OP_INVOKESUPER_QUICK,         s)
			.setDispatchStrategy(OP_INVOKEVIRTUAL_QUICK,       s)
			.setDispatchStrategy(OP_INVOKEVIRTUAL_QUICK_W,     s)
			.setDispatchStrategy(OP_INVOKEVIRTUALOBJECT_QUICK, s)
			.setDispatchStrategy(OP_LDC_QUICK,                 s)
			.setDispatchStrategy(OP_LDC_W_QUICK,               s)
			.setDispatchStrategy(OP_LDC2_W_QUICK,              s)
			.setDispatchStrategy(OP_MULTIANEWARRAY_QUICK,      s)
			.setDispatchStrategy(OP_NEW_QUICK,                 s)
			.setDispatchStrategy(OP_PUTFIELD_QUICK,            s)
			.setDispatchStrategy(OP_PUTFIELD_QUICK_W,          s)
			.setDispatchStrategy(OP_PUTFIELD2_QUICK,           s)
			.setDispatchStrategy(OP_PUTSTATIC_QUICK,           s)
			.setDispatchStrategy(OP_PUTSTATIC2_QUICK,          s)
		;
	}
	
	@Override
	public FormatterStrategy select(Byte bytecode) {
		final FormatterStrategy retVal;
		try {
			retVal = super.select(bytecode);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		return retVal;
	}
}
