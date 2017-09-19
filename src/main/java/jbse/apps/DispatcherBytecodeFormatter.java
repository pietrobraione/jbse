package jbse.apps;

import static jbse.bc.Opcodes.*;

import java.util.function.BiFunction;

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

/**
 * This interface exists only to simplify naming. 
 * 
 * @author Pietro Braione
 *
 */
interface TextGenerator
extends BiFunction<Frame, ClassHierarchy, String> {
    //empty
}

/**
 * This interface exists only to simplify naming. 
 * 
 * @author Pietro Braione
 *
 */
interface DispatchStrategyFormat 
extends Dispatcher.DispatchStrategy<TextGenerator> {
    //empty
}

class DispatcherBytecodeFormatter extends Dispatcher<Byte, TextGenerator> {
    private final static String UNRECOGNIZED_BYTECODE = "<???>";
    private final static String INCORRECT_BYTECODE    = "<INCORRECT>";
    private final static String RESERVED_BYTECODE     = "<RESERVED>";

    /**
     * A formatter for bytecodes with 0 operands, which just returns their name.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat0 implements DispatchStrategyFormat {
        private final String text;
        
        public DispatchStrategyFormat0(String text) { 
            this.text = text;
        }
        
        public TextGenerator doIt() {
            return (f, hier) -> DispatchStrategyFormat0.this.text; 
        }
    }

    /**
     * A formatter for bytecodes with 0 operands, which returns their name plus a local
     * variable whose index is specified in the constructor.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat0LV implements DispatchStrategyFormat {
        private final String text;
        private final int slot;
        
        public DispatchStrategyFormat0LV(String text, int slot) { 
            this.text = text; 
            this.slot = slot; 
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> { 
                final String varName = f.getLocalVariableDeclaredName(DispatchStrategyFormat0LV.this.slot);
                return DispatchStrategyFormat0LV.this.text + (varName == null ? "" : " [" + varName + "]"); 
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) whose
     * meaning is a primitive type in newarray coding.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1AT implements DispatchStrategyFormat {
        private final String text;
        
        public DispatchStrategyFormat1AT(String text) {
            this.text = text;
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> { 
                String retVal = DispatchStrategyFormat1AT.this.text + " ";
                try {
                    final short UB = f.getInstruction(1);
                    final String type;
                    switch (Array.arrayPrimitiveType(UB)) {
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
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type signed byte (8 bits).
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1SB implements DispatchStrategyFormat {
        private final String text;
        public DispatchStrategyFormat1SB(String text) { this.text = text; }
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> { 
                String retVal = DispatchStrategyFormat1SB.this.text + " ";
                try {
                    final byte SB = f.getInstruction(1);
                    retVal += SB;
                } catch (InvalidProgramCounterException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;					
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type signed word (16 bits).
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1SW implements DispatchStrategyFormat {
        private final String text;
        public DispatchStrategyFormat1SW(String text) { this.text = text; }
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat1SW.this.text + " ";
                try {
                    final short SW = Util.byteCatShort(f.getInstruction(1), f.getInstruction(2));
                    retVal += SW;
                } catch (InvalidProgramCounterException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) or unsigned 
     * word (16 bits) whose meaning is the index of a local variable.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1LV implements DispatchStrategyFormat {
        private final String text;
        public DispatchStrategyFormat1LV(String text) { this.text = text; }
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat1LV.this.text + " ";
                try {
                    //determines whether the operand is wide
                    boolean wide = false;
                    try {
                        final byte prev = f.getInstruction(-1);
                        wide = (prev == OP_WIDE);
                    } catch (InvalidProgramCounterException e) {
                        //does nothing (this is the first bytecode in the method)
                    }
                    final int UW;
                    if (wide) {
                        UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
                    } else {
                        UW = f.getInstruction(1);
                    }
                    final String varName = f.getLocalVariableDeclaredName(UW);
                    retVal += (varName == null ? UW : varName + " [" + UW + "]");
                } catch (InvalidProgramCounterException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type signed word (16 bits) whose meaning 
     * is a (near) jump offset.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1ON implements DispatchStrategyFormat {
        private final String text;
        
        public DispatchStrategyFormat1ON(String text) { 
            this.text = text; 
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat1ON.this.text + " ";
                try {
                    final short SW = Util.byteCatShort(f.getInstruction(1), f.getInstruction(2));
                    final int target = f.getProgramCounter() + SW;
                    retVal += target;
                } catch (InvalidProgramCounterException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type signed dword (32 bits) 
     * whose meaning is a (far) jump offset.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1OF implements DispatchStrategyFormat {
        private final String text;
        
        public DispatchStrategyFormat1OF(String text) {
            this.text = text;
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat1OF.this.text + " ";
                try {
                    final int SD = Util.byteCat(f.getInstruction(1), f.getInstruction(2), f.getInstruction(3), f.getInstruction(4));
                    final int target = f.getProgramCounter() + SD;
                    retVal += target;
                } catch (InvalidProgramCounterException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) 
     * or unsigned word (16 bits) whose meaning is a literal constant in the constant pool.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1CO implements DispatchStrategyFormat {
        private final String text;
        private final boolean wide;
        
        public DispatchStrategyFormat1CO(String text, boolean wide) { 
            this.text = text; 
            this.wide = wide;
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
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
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
     * meaning is a class/array/interface signature in the constant pool.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1CL implements DispatchStrategyFormat {
        private final String text;
        
        public DispatchStrategyFormat1CL(String text) {
            this.text = text;
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> { 
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
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
     * meaning is a field signature in the constant pool.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1FI implements DispatchStrategyFormat {
        private final String text;
        
        public DispatchStrategyFormat1FI(String text) {
            this.text = text;
        }
        
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
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
            };
        }		
    }

    /**
     * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
     * meaning is a method signature in the constant pool.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1ME implements DispatchStrategyFormat {
        private final String text;
        private final boolean isInterface;

        public DispatchStrategyFormat1ME(String text, boolean isInterface) { 
            this.text = text; 
            this.isInterface = isInterface;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
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
            };
        }		
    }

    /**
     * A formatter for bytecodes with 2 operands, the first with type unsigned word (16 bits) 
     * whose meaning is a class/array/interface signature in the constant pool, the second
     * with type unsigned byte (8 bits).
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat2CLUB implements DispatchStrategyFormat {
        private final String text;

        public DispatchStrategyFormat2CLUB(String text) {
            this.text = text;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
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
    private static class DispatchStrategyFormat2LVIM implements DispatchStrategyFormat {
        private final String text;

        public DispatchStrategyFormat2LVIM(String text) {
            this.text = text;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
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
                    final String varName = f.getLocalVariableDeclaredName(UW0);
                    retVal += UW0 + " " + UW1 + (varName == null ? "" : " [" + varName + "]");
                } catch (InvalidProgramCounterException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }

    /**
     * A formatter for *switch bytecodes.
     *
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormatSWITCH implements DispatchStrategyFormat {
        private final String text;
        private final boolean isTableSwitch;

        public DispatchStrategyFormatSWITCH(String text, boolean isTableSwitch) {
            this.text = text; this.isTableSwitch = isTableSwitch;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormatSWITCH.this.text + " ";
                SwitchTable tab;
                try {
                    tab = new SwitchTable(f, null, DispatchStrategyFormatSWITCH.this.isTableSwitch);
                    final StringBuilder buf = new StringBuilder();
                    for (int val : tab) {
                        final int target = f.getProgramCounter() + tab.jumpOffset(val);
                        buf.append(val);
                        buf.append(":");
                        buf.append(target);
                        buf.append(" ");
                    }
                    retVal += buf.toString();
                    final int target = f.getProgramCounter() + tab.jumpOffsetDefault();
                    retVal += "dflt:" + target;
                } catch (InvalidProgramCounterException e) {
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }

    DispatcherBytecodeFormatter() {
        setDefault(new DispatchStrategyFormat0(INCORRECT_BYTECODE));
        setCase(OP_NOP,             new DispatchStrategyFormat0("NOP"));
        setCase(OP_ALOAD,           new DispatchStrategyFormat1LV("ALOAD"));
        setCase(OP_DLOAD,           new DispatchStrategyFormat1LV("DLOAD"));
        setCase(OP_FLOAD,           new DispatchStrategyFormat1LV("FLOAD"));
        setCase(OP_ILOAD,           new DispatchStrategyFormat1LV("ILOAD"));
        setCase(OP_LLOAD,           new DispatchStrategyFormat1LV("LLOAD"));
        setCase(OP_ALOAD_0,         new DispatchStrategyFormat0LV("ALOAD_0", 0));
        setCase(OP_DLOAD_0,         new DispatchStrategyFormat0LV("DLOAD_0", 0));
        setCase(OP_FLOAD_0,         new DispatchStrategyFormat0LV("FLOAD_0", 0));
        setCase(OP_ILOAD_0,         new DispatchStrategyFormat0LV("ILOAD_0", 0));
        setCase(OP_LLOAD_0,         new DispatchStrategyFormat0LV("LLOAD_0", 0));
        setCase(OP_ALOAD_1,         new DispatchStrategyFormat0LV("ALOAD_1", 1));
        setCase(OP_DLOAD_1,         new DispatchStrategyFormat0LV("DLOAD_1", 1));
        setCase(OP_FLOAD_1,         new DispatchStrategyFormat0LV("FLOAD_1", 1));
        setCase(OP_ILOAD_1,         new DispatchStrategyFormat0LV("ILOAD_1", 1));
        setCase(OP_LLOAD_1,         new DispatchStrategyFormat0LV("LLOAD_1", 1));
        setCase(OP_ALOAD_2,         new DispatchStrategyFormat0LV("ALOAD_2", 2));
        setCase(OP_DLOAD_2,         new DispatchStrategyFormat0LV("DLOAD_2", 2));
        setCase(OP_FLOAD_2,         new DispatchStrategyFormat0LV("FLOAD_2", 2));
        setCase(OP_ILOAD_2,         new DispatchStrategyFormat0LV("ILOAD_2", 2));
        setCase(OP_LLOAD_2,         new DispatchStrategyFormat0LV("LLOAD_2", 2));
        setCase(OP_ALOAD_3,         new DispatchStrategyFormat0LV("ALOAD_3", 3));
        setCase(OP_DLOAD_3,         new DispatchStrategyFormat0LV("DLOAD_3", 3));
        setCase(OP_FLOAD_3,         new DispatchStrategyFormat0LV("FLOAD_3", 3));
        setCase(OP_ILOAD_3,         new DispatchStrategyFormat0LV("ILOAD_3", 3));
        setCase(OP_LLOAD_3,         new DispatchStrategyFormat0LV("LLOAD_3", 3));
        setCase(OP_ASTORE,          new DispatchStrategyFormat1LV("ASTORE"));
        setCase(OP_DSTORE,          new DispatchStrategyFormat1LV("DSTORE"));
        setCase(OP_FSTORE,          new DispatchStrategyFormat1LV("FSTORE"));
        setCase(OP_ISTORE,          new DispatchStrategyFormat1LV("ISTORE"));
        setCase(OP_LSTORE,          new DispatchStrategyFormat1LV("LSTORE"));
        setCase(OP_ASTORE_0,        new DispatchStrategyFormat0LV("ASTORE_0", 0));
        setCase(OP_DSTORE_0,        new DispatchStrategyFormat0LV("DSTORE_0", 0));
        setCase(OP_FSTORE_0,        new DispatchStrategyFormat0LV("FSTORE_0", 0));
        setCase(OP_ISTORE_0,        new DispatchStrategyFormat0LV("ISTORE_0", 0));
        setCase(OP_LSTORE_0,        new DispatchStrategyFormat0LV("LSTORE_0", 0));
        setCase(OP_ASTORE_1,        new DispatchStrategyFormat0LV("ASTORE_1", 1));
        setCase(OP_DSTORE_1,        new DispatchStrategyFormat0LV("DSTORE_1", 1));
        setCase(OP_FSTORE_1,        new DispatchStrategyFormat0LV("FSTORE_1", 1));
        setCase(OP_ISTORE_1,        new DispatchStrategyFormat0LV("ISTORE_1", 1));
        setCase(OP_LSTORE_1,        new DispatchStrategyFormat0LV("LSTORE_1", 1));
        setCase(OP_ASTORE_2,        new DispatchStrategyFormat0LV("ASTORE_2", 2));
        setCase(OP_DSTORE_2,        new DispatchStrategyFormat0LV("DSTORE_2", 2));
        setCase(OP_FSTORE_2,        new DispatchStrategyFormat0LV("FSTORE_2", 2));
        setCase(OP_ISTORE_2,        new DispatchStrategyFormat0LV("ISTORE_2", 2));
        setCase(OP_LSTORE_2,        new DispatchStrategyFormat0LV("LSTORE_2", 2));
        setCase(OP_ASTORE_3,        new DispatchStrategyFormat0LV("ASTORE_3", 3));
        setCase(OP_DSTORE_3,        new DispatchStrategyFormat0LV("DSTORE_3", 3));
        setCase(OP_FSTORE_3,        new DispatchStrategyFormat0LV("FSTORE_3", 3));
        setCase(OP_ISTORE_3,        new DispatchStrategyFormat0LV("ISTORE_3", 3));
        setCase(OP_LSTORE_3,        new DispatchStrategyFormat0LV("LSTORE_3", 3));
        setCase(OP_BIPUSH,          new DispatchStrategyFormat1SB("BIPUSH"));
        setCase(OP_SIPUSH,          new DispatchStrategyFormat1SW("SIPUSH"));
        setCase(OP_LDC,             new DispatchStrategyFormat1CO("LDC", false));
        setCase(OP_LDC_W,           new DispatchStrategyFormat1CO("LDC_W", true));
        setCase(OP_LDC2_W,          new DispatchStrategyFormat1CO("LDC2_W", true));
        setCase(OP_ACONST_NULL,     new DispatchStrategyFormat0("ACONST_NULL"));
        setCase(OP_DCONST_0,        new DispatchStrategyFormat0("DCONST_0"));
        setCase(OP_DCONST_1,        new DispatchStrategyFormat0("DCONST_1"));
        setCase(OP_FCONST_0,        new DispatchStrategyFormat0("FCONST_0"));
        setCase(OP_FCONST_1,        new DispatchStrategyFormat0("FCONST_1"));
        setCase(OP_FCONST_2,        new DispatchStrategyFormat0("FCONST_2"));
        setCase(OP_ICONST_M1,       new DispatchStrategyFormat0("ICONST_M1"));
        setCase(OP_ICONST_0,        new DispatchStrategyFormat0("ICONST_0"));
        setCase(OP_ICONST_1,        new DispatchStrategyFormat0("ICONST_1"));
        setCase(OP_ICONST_2,        new DispatchStrategyFormat0("ICONST_2"));
        setCase(OP_ICONST_3,        new DispatchStrategyFormat0("ICONST_3"));
        setCase(OP_ICONST_4,        new DispatchStrategyFormat0("ICONST_4"));
        setCase(OP_ICONST_5,        new DispatchStrategyFormat0("ICONST_5"));
        setCase(OP_LCONST_0,        new DispatchStrategyFormat0("LCONST_0"));
        setCase(OP_LCONST_1,        new DispatchStrategyFormat0("LCONST_1"));
        setCase(OP_DADD,            new DispatchStrategyFormat0("DADD"));
        setCase(OP_FADD,            new DispatchStrategyFormat0("FADD"));
        setCase(OP_IADD,            new DispatchStrategyFormat0("IADD"));
        setCase(OP_LADD,            new DispatchStrategyFormat0("LADD"));
        setCase(OP_DSUB,            new DispatchStrategyFormat0("DSUB"));
        setCase(OP_FSUB,            new DispatchStrategyFormat0("FSUB"));
        setCase(OP_ISUB,            new DispatchStrategyFormat0("ISUB"));
        setCase(OP_LSUB,            new DispatchStrategyFormat0("LSUB"));
        setCase(OP_DMUL,            new DispatchStrategyFormat0("DMUL"));
        setCase(OP_FMUL,            new DispatchStrategyFormat0("FMUL"));
        setCase(OP_IMUL,            new DispatchStrategyFormat0("IMUL"));
        setCase(OP_LMUL,            new DispatchStrategyFormat0("LMUL"));
        setCase(OP_DDIV,            new DispatchStrategyFormat0("DDIV"));
        setCase(OP_FDIV,            new DispatchStrategyFormat0("FDIV"));
        setCase(OP_IDIV,            new DispatchStrategyFormat0("IDIV"));
        setCase(OP_LDIV,            new DispatchStrategyFormat0("LDIV"));
        setCase(OP_DREM,            new DispatchStrategyFormat0("DREM"));
        setCase(OP_FREM,            new DispatchStrategyFormat0("FREM"));
        setCase(OP_IREM,            new DispatchStrategyFormat0("IREM"));
        setCase(OP_LREM,            new DispatchStrategyFormat0("LREM"));
        setCase(OP_DNEG,            new DispatchStrategyFormat0("DNEG"));
        setCase(OP_FNEG,            new DispatchStrategyFormat0("FNEG"));
        setCase(OP_INEG,            new DispatchStrategyFormat0("INEG"));
        setCase(OP_LNEG,            new DispatchStrategyFormat0("LNEG"));
        setCase(OP_ISHL,            new DispatchStrategyFormat0("ISHL"));
        setCase(OP_LSHL,            new DispatchStrategyFormat0("LSHL"));
        setCase(OP_ISHR,            new DispatchStrategyFormat0("ISHR"));
        setCase(OP_LSHR,            new DispatchStrategyFormat0("LSHR"));
        setCase(OP_IUSHR,           new DispatchStrategyFormat0("IUSHL"));
        setCase(OP_LUSHR,           new DispatchStrategyFormat0("LUSHL"));
        setCase(OP_IOR,             new DispatchStrategyFormat0("IOR"));
        setCase(OP_LOR,             new DispatchStrategyFormat0("LOR"));
        setCase(OP_IAND,            new DispatchStrategyFormat0("IAND"));
        setCase(OP_LAND,            new DispatchStrategyFormat0("LAND"));
        setCase(OP_IXOR,            new DispatchStrategyFormat0("IXOR"));
        setCase(OP_LXOR,            new DispatchStrategyFormat0("LXOR"));
        setCase(OP_IINC,            new DispatchStrategyFormat2LVIM("IINC"));
        setCase(OP_DCMPG,           new DispatchStrategyFormat0("DCMPG"));
        setCase(OP_DCMPL,           new DispatchStrategyFormat0("DCMPL"));
        setCase(OP_FCMPG,           new DispatchStrategyFormat0("FCMPG"));
        setCase(OP_FCMPL,           new DispatchStrategyFormat0("FCMPL"));
        setCase(OP_LCMP,            new DispatchStrategyFormat0("LCMP"));
        setCase(OP_I2D,             new DispatchStrategyFormat0("I2D"));
        setCase(OP_I2F,             new DispatchStrategyFormat0("I2F"));
        setCase(OP_I2L,             new DispatchStrategyFormat0("I2L"));
        setCase(OP_L2D,             new DispatchStrategyFormat0("L2D"));
        setCase(OP_L2F,             new DispatchStrategyFormat0("L2F"));
        setCase(OP_F2D,             new DispatchStrategyFormat0("F2D"));
        setCase(OP_D2F,             new DispatchStrategyFormat0("D2F"));
        setCase(OP_D2I,             new DispatchStrategyFormat0("D2I"));
        setCase(OP_D2L,             new DispatchStrategyFormat0("D2L"));
        setCase(OP_F2I,             new DispatchStrategyFormat0("F2I"));
        setCase(OP_F2L,             new DispatchStrategyFormat0("F2L"));
        setCase(OP_I2B,             new DispatchStrategyFormat0("I2B"));
        setCase(OP_I2C,             new DispatchStrategyFormat0("I2C"));
        setCase(OP_I2S,             new DispatchStrategyFormat0("I2S"));
        setCase(OP_L2I,             new DispatchStrategyFormat0("L2I"));
        setCase(OP_NEW,             new DispatchStrategyFormat1CL("NEW"));
        setCase(OP_NEWARRAY,        new DispatchStrategyFormat1AT("NEWARRAY"));
        setCase(OP_ANEWARRAY,       new DispatchStrategyFormat1CL("ANEWARRAY"));
        setCase(OP_MULTIANEWARRAY,  new DispatchStrategyFormat2CLUB("MULTIANEWARRAY"));
        setCase(OP_GETFIELD,        new DispatchStrategyFormat1FI("GETFIELD"));
        setCase(OP_PUTFIELD,        new DispatchStrategyFormat1FI("PUTFIELD"));
        setCase(OP_GETSTATIC,       new DispatchStrategyFormat1FI("GETSTATIC"));
        setCase(OP_PUTSTATIC,       new DispatchStrategyFormat1FI("PUTSTATIC"));
        setCase(OP_AALOAD,          new DispatchStrategyFormat0("AALOAD"));
        setCase(OP_CALOAD,          new DispatchStrategyFormat0("CALOAD"));
        setCase(OP_BALOAD,          new DispatchStrategyFormat0("BALOAD"));
        setCase(OP_DALOAD,          new DispatchStrategyFormat0("DALOAD"));
        setCase(OP_FALOAD,          new DispatchStrategyFormat0("FALOAD"));
        setCase(OP_IALOAD,          new DispatchStrategyFormat0("IALOAD"));
        setCase(OP_LALOAD,          new DispatchStrategyFormat0("LALOAD"));
        setCase(OP_SALOAD,          new DispatchStrategyFormat0("SALOAD"));
        setCase(OP_AASTORE,         new DispatchStrategyFormat0("AASTORE"));
        setCase(OP_CASTORE,         new DispatchStrategyFormat0("CASTORE"));
        setCase(OP_BASTORE,         new DispatchStrategyFormat0("BASTORE"));
        setCase(OP_DASTORE,         new DispatchStrategyFormat0("DASTORE"));
        setCase(OP_FASTORE,         new DispatchStrategyFormat0("FASTORE"));
        setCase(OP_IASTORE,         new DispatchStrategyFormat0("IASTORE"));
        setCase(OP_LASTORE,         new DispatchStrategyFormat0("LASTORE"));
        setCase(OP_SASTORE,         new DispatchStrategyFormat0("SASTORE"));
        setCase(OP_ARRAYLENGTH,     new DispatchStrategyFormat0("ARRAYLENGTH"));
        setCase(OP_INSTANCEOF,      new DispatchStrategyFormat1CL("INSTANCEOF"));
        setCase(OP_CHECKCAST,       new DispatchStrategyFormat1CL("CHECKCAST"));
        setCase(OP_POP,             new DispatchStrategyFormat0("POP"));
        setCase(OP_POP2,            new DispatchStrategyFormat0("POP2"));
        setCase(OP_DUP,             new DispatchStrategyFormat0("DUP"));
        setCase(OP_DUP2,            new DispatchStrategyFormat0("DUP2"));
        setCase(OP_DUP_X1,          new DispatchStrategyFormat0("DUP_X1"));
        setCase(OP_DUP2_X1,         new DispatchStrategyFormat0("DUP2_X1"));
        setCase(OP_DUP_X2,          new DispatchStrategyFormat0("DUP_X2"));
        setCase(OP_DUP2_X2,         new DispatchStrategyFormat0("DUP2_X2"));
        setCase(OP_SWAP,            new DispatchStrategyFormat0("SWAP"));
        setCase(OP_IFEQ,            new DispatchStrategyFormat1ON("IFEQ"));
        setCase(OP_IFGE,            new DispatchStrategyFormat1ON("IFGE"));
        setCase(OP_IFGT,            new DispatchStrategyFormat1ON("IFGT"));
        setCase(OP_IFLE,            new DispatchStrategyFormat1ON("IFLE"));
        setCase(OP_IFLT,            new DispatchStrategyFormat1ON("IFLT"));
        setCase(OP_IFNE,            new DispatchStrategyFormat1ON("IFNE"));
        setCase(OP_IFNONNULL,       new DispatchStrategyFormat1ON("IFNONNULL"));
        setCase(OP_IFNULL,          new DispatchStrategyFormat1ON("IFNULL"));
        setCase(OP_IF_ICMPEQ,       new DispatchStrategyFormat1ON("IF_ICMPEQ"));
        setCase(OP_IF_ICMPGE,       new DispatchStrategyFormat1ON("IF_ICMPGE"));
        setCase(OP_IF_ICMPGT,       new DispatchStrategyFormat1ON("IF_ICMPGT"));
        setCase(OP_IF_ICMPLE,       new DispatchStrategyFormat1ON("IF_ICMPLE"));
        setCase(OP_IF_ICMPLT,       new DispatchStrategyFormat1ON("IF_ICMPLT"));
        setCase(OP_IF_ICMPNE,       new DispatchStrategyFormat1ON("IF_ICMPNE"));
        setCase(OP_IF_ACMPEQ,       new DispatchStrategyFormat1ON("IF_ACMPEQ"));
        setCase(OP_IF_ACMPNE,       new DispatchStrategyFormat1ON("IF_ACMPNE"));
        setCase(OP_TABLESWITCH,     new DispatchStrategyFormatSWITCH("TABLESWITCH", true));
        setCase(OP_LOOKUPSWITCH,    new DispatchStrategyFormatSWITCH("LOOKUPSWITCH", false));
        setCase(OP_GOTO,            new DispatchStrategyFormat1ON("GOTO"));
        setCase(OP_GOTO_W,          new DispatchStrategyFormat1OF("GOTO_W"));
        setCase(OP_JSR,             new DispatchStrategyFormat1ON("JSR"));
        setCase(OP_JSR_W,           new DispatchStrategyFormat1OF("JSR_W"));
        setCase(OP_RET,             new DispatchStrategyFormat1LV("RET"));
        setCase(OP_INVOKEINTERFACE, new DispatchStrategyFormat1ME("INVOKEINTERFACE", true));
        setCase(OP_INVOKEVIRTUAL,   new DispatchStrategyFormat1ME("INVOKEVIRTUAL", false));
        setCase(OP_INVOKESPECIAL,   new DispatchStrategyFormat1ME("INVOKESPECIAL", false));
        setCase(OP_INVOKESTATIC,    new DispatchStrategyFormat1ME("INVOKESTATIC", false));
        setCase(OP_RETURN,          new DispatchStrategyFormat0("RETURN"));
        setCase(OP_ARETURN,         new DispatchStrategyFormat0("ARETURN"));
        setCase(OP_DRETURN,         new DispatchStrategyFormat0("DRETURN"));
        setCase(OP_FRETURN,         new DispatchStrategyFormat0("FRETURN"));
        setCase(OP_IRETURN,         new DispatchStrategyFormat0("IRETURN"));
        setCase(OP_LRETURN,         new DispatchStrategyFormat0("LRETURN"));
        setCase(OP_ATHROW,          new DispatchStrategyFormat0("ATHROW"));
        setCase(OP_WIDE,            new DispatchStrategyFormat0("WIDE"));
        setCase(OP_MONITORENTER,    new DispatchStrategyFormat0("MONITORENTER"));
        setCase(OP_MONITOREXIT,     new DispatchStrategyFormat0("MONITOREXIT"));
        setCase(OP_BREAKPOINT,      new DispatchStrategyFormat0("BREAKPOINT"));
        setCase(OP_IMPDEP1,         new DispatchStrategyFormat0("IMPDEP1"));
        setCase(OP_IMPDEP2,         new DispatchStrategyFormat0("IMPDEP2"));

        final DispatchStrategyFormat0 s = new DispatchStrategyFormat0(RESERVED_BYTECODE);
        setCase(OP_INVOKEDYNAMIC,             s);
        setCase(OP_ANEWARRAY_QUICK,           s);
        setCase(OP_CHECKCAST_QUICK,           s);
        setCase(OP_GETSTATIC_QUICK,           s);
        setCase(OP_GETSTATIC2_QUICK,          s);
        setCase(OP_GETFIELD_QUICK,            s);
        setCase(OP_GETFIELD_QUICK_W,          s);
        setCase(OP_GETFIELD2_QUICK,           s);
        setCase(OP_INSTANCEOF_QUICK,          s);
        setCase(OP_INVOKEINTERFACE_QUICK,     s);
        setCase(OP_INVOKENONVIRTUAL_QUICK,    s);
        setCase(OP_INVOKESTATIC_QUICK,        s);
        setCase(OP_INVOKESUPER_QUICK,         s);
        setCase(OP_INVOKEVIRTUAL_QUICK,       s);
        setCase(OP_INVOKEVIRTUAL_QUICK_W,     s);
        setCase(OP_INVOKEVIRTUALOBJECT_QUICK, s);
        setCase(OP_LDC_QUICK,                 s);
        setCase(OP_LDC_W_QUICK,               s);
        setCase(OP_LDC2_W_QUICK,              s);
        setCase(OP_MULTIANEWARRAY_QUICK,      s);
        setCase(OP_NEW_QUICK,                 s);
        setCase(OP_PUTFIELD_QUICK,            s);
        setCase(OP_PUTFIELD_QUICK_W,          s);
        setCase(OP_PUTFIELD2_QUICK,           s);
        setCase(OP_PUTSTATIC_QUICK,           s);
        setCase(OP_PUTSTATIC2_QUICK,          s);
    }

    @Override
    public TextGenerator select(Byte bytecode) {
        final TextGenerator retVal;
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
