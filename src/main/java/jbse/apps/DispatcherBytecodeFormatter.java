package jbse.apps;

import static jbse.bc.Opcodes.*;
import static jbse.common.Util.asUnsignedByte;

import java.util.function.BiFunction;

import jbse.bc.CallSiteSpecifier;
import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Dispatcher;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileIllFormedException;
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
    private static class DispatchStrategyFormat1ZUX implements DispatchStrategyFormat {
        private final String text;
        private final boolean wide;

        public DispatchStrategyFormat1ZUX(String text, boolean wide) { 
            this.text = text; 
            this.wide = wide;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat1ZUX.this.text + " ";
                try {
                    final int UW;
                    if (this.wide) {
                        UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
                    } else {
                        UW = asUnsignedByte(f.getInstruction(1));
                    }
                    final ConstantPoolValue cpVal = 
                        f.getMethodClass().getValueFromConstantPool(UW);
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
                         ClassFileIllFormedException | UnexpectedInternalException e) {
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
                    final String sig = f.getMethodClass().getClassSignature(UW);
                    retVal += sig;
                } catch (InvalidProgramCounterException | InvalidIndexException |
                         UnexpectedInternalException e) {
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
                    final Signature sig = f.getMethodClass().getFieldSignature(UW);
                    retVal += sig.getClassName() + Signature.SIGNATURE_SEPARATOR + sig.getName() + " [" + UW + "]";
                } catch (InvalidProgramCounterException | InvalidIndexException |
                         UnexpectedInternalException e) {
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
                    if (DispatchStrategyFormat1ME.this.isInterface) {
                        sig = f.getMethodClass().getInterfaceMethodSignature(UW);
                    } else {
                        sig = f.getMethodClass().getMethodSignature(UW);
                    }
                    retVal += sig + " [" + UW + "]";
                } catch (InvalidProgramCounterException | InvalidIndexException |
                         UnexpectedInternalException e) {
                    //unrecognized bytecode
                    retVal += UNRECOGNIZED_BYTECODE;
                }
                return retVal;
            };
        }		
    }


    /**
     * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
     * meaning is a call site specifier in the constant pool.
     * 
     * @author Pietro Braione
     */
    private static class DispatchStrategyFormat1CS implements DispatchStrategyFormat {
        private final String text;
        public DispatchStrategyFormat1CS(String text) { this.text = text; }
        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> { 
                String retVal = DispatchStrategyFormat1CS.this.text + " ";
                try {
                    final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
                    final CallSiteSpecifier css = f.getMethodClass().getCallSiteSpecifier(UW);
                    retVal += " " + css.getDescriptor() + ":" + css.getName() + " * " + css.getBootstrapMethodSignature().toString(); 
                } catch (InvalidProgramCounterException | InvalidIndexException | 
                		ClassFileIllFormedException e) {
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
                    final String sig = f.getMethodClass().getClassSignature(UW);
                    retVal += sig + " " + UB + " [" + UW + "]";
                } catch (InvalidProgramCounterException | InvalidIndexException |
                         UnexpectedInternalException e) {
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
    private static class DispatchStrategyFormat2LVSX implements DispatchStrategyFormat {
        private final String text;

        public DispatchStrategyFormat2LVSX(String text) {
            this.text = text;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat2LVSX.this.text + " ";
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
    private static class DispatchStrategyFormat1ZSWITCH implements DispatchStrategyFormat {
        private final String text;
        private final boolean isTableSwitch;

        public DispatchStrategyFormat1ZSWITCH(String text, boolean isTableSwitch) {
            this.text = text; this.isTableSwitch = isTableSwitch;
        }

        public TextGenerator doIt() {
            return (Frame f, ClassHierarchy hier) -> {
                String retVal = DispatchStrategyFormat1ZSWITCH.this.text + " ";
                SwitchTable tab;
                try {
                    tab = new SwitchTable(f, DispatchStrategyFormat1ZSWITCH.this.isTableSwitch);
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
        setDefault(new DispatchStrategyFormat0(opcodeName(OP_INCORRECT)));
        setCase(OP_NOP,             new DispatchStrategyFormat0(opcodeName(OP_NOP)));
        setCase(OP_ALOAD,           new DispatchStrategyFormat1LV(opcodeName(OP_ALOAD)));
        setCase(OP_DLOAD,           new DispatchStrategyFormat1LV(opcodeName(OP_DLOAD)));
        setCase(OP_FLOAD,           new DispatchStrategyFormat1LV(opcodeName(OP_FLOAD)));
        setCase(OP_ILOAD,           new DispatchStrategyFormat1LV(opcodeName(OP_ILOAD)));
        setCase(OP_LLOAD,           new DispatchStrategyFormat1LV(opcodeName(OP_LLOAD)));
        setCase(OP_ALOAD_0,         new DispatchStrategyFormat0LV(opcodeName(OP_ALOAD_0), 0));
        setCase(OP_DLOAD_0,         new DispatchStrategyFormat0LV(opcodeName(OP_DLOAD_0), 0));
        setCase(OP_FLOAD_0,         new DispatchStrategyFormat0LV(opcodeName(OP_FLOAD_0), 0));
        setCase(OP_ILOAD_0,         new DispatchStrategyFormat0LV(opcodeName(OP_ILOAD_0), 0));
        setCase(OP_LLOAD_0,         new DispatchStrategyFormat0LV(opcodeName(OP_LLOAD_0), 0));
        setCase(OP_ALOAD_1,         new DispatchStrategyFormat0LV(opcodeName(OP_ALOAD_1), 1));
        setCase(OP_DLOAD_1,         new DispatchStrategyFormat0LV(opcodeName(OP_DLOAD_1), 1));
        setCase(OP_FLOAD_1,         new DispatchStrategyFormat0LV(opcodeName(OP_FLOAD_1), 1));
        setCase(OP_ILOAD_1,         new DispatchStrategyFormat0LV(opcodeName(OP_ILOAD_1), 1));
        setCase(OP_LLOAD_1,         new DispatchStrategyFormat0LV(opcodeName(OP_LLOAD_1), 1));
        setCase(OP_ALOAD_2,         new DispatchStrategyFormat0LV(opcodeName(OP_ALOAD_2), 2));
        setCase(OP_DLOAD_2,         new DispatchStrategyFormat0LV(opcodeName(OP_DLOAD_2), 2));
        setCase(OP_FLOAD_2,         new DispatchStrategyFormat0LV(opcodeName(OP_FLOAD_2), 2));
        setCase(OP_ILOAD_2,         new DispatchStrategyFormat0LV(opcodeName(OP_ILOAD_2), 2));
        setCase(OP_LLOAD_2,         new DispatchStrategyFormat0LV(opcodeName(OP_LLOAD_2), 2));
        setCase(OP_ALOAD_3,         new DispatchStrategyFormat0LV(opcodeName(OP_ALOAD_3), 3));
        setCase(OP_DLOAD_3,         new DispatchStrategyFormat0LV(opcodeName(OP_DLOAD_3), 3));
        setCase(OP_FLOAD_3,         new DispatchStrategyFormat0LV(opcodeName(OP_FLOAD_3), 3));
        setCase(OP_ILOAD_3,         new DispatchStrategyFormat0LV(opcodeName(OP_ILOAD_3), 3));
        setCase(OP_LLOAD_3,         new DispatchStrategyFormat0LV(opcodeName(OP_LLOAD_3), 3));
        setCase(OP_ASTORE,          new DispatchStrategyFormat1LV(opcodeName(OP_ASTORE)));
        setCase(OP_DSTORE,          new DispatchStrategyFormat1LV(opcodeName(OP_DSTORE)));
        setCase(OP_FSTORE,          new DispatchStrategyFormat1LV(opcodeName(OP_FSTORE)));
        setCase(OP_ISTORE,          new DispatchStrategyFormat1LV(opcodeName(OP_ISTORE)));
        setCase(OP_LSTORE,          new DispatchStrategyFormat1LV(opcodeName(OP_LSTORE)));
        setCase(OP_ASTORE_0,        new DispatchStrategyFormat0LV(opcodeName(OP_ASTORE_0), 0));
        setCase(OP_DSTORE_0,        new DispatchStrategyFormat0LV(opcodeName(OP_DSTORE_0), 0));
        setCase(OP_FSTORE_0,        new DispatchStrategyFormat0LV(opcodeName(OP_FSTORE_0), 0));
        setCase(OP_ISTORE_0,        new DispatchStrategyFormat0LV(opcodeName(OP_ISTORE_0), 0));
        setCase(OP_LSTORE_0,        new DispatchStrategyFormat0LV(opcodeName(OP_LSTORE_0), 0));
        setCase(OP_ASTORE_1,        new DispatchStrategyFormat0LV(opcodeName(OP_ASTORE_1), 1));
        setCase(OP_DSTORE_1,        new DispatchStrategyFormat0LV(opcodeName(OP_DSTORE_1), 1));
        setCase(OP_FSTORE_1,        new DispatchStrategyFormat0LV(opcodeName(OP_FSTORE_1), 1));
        setCase(OP_ISTORE_1,        new DispatchStrategyFormat0LV(opcodeName(OP_ISTORE_1), 1));
        setCase(OP_LSTORE_1,        new DispatchStrategyFormat0LV(opcodeName(OP_LSTORE_1), 1));
        setCase(OP_ASTORE_2,        new DispatchStrategyFormat0LV(opcodeName(OP_ASTORE_2), 2));
        setCase(OP_DSTORE_2,        new DispatchStrategyFormat0LV(opcodeName(OP_DSTORE_2), 2));
        setCase(OP_FSTORE_2,        new DispatchStrategyFormat0LV(opcodeName(OP_FSTORE_2), 2));
        setCase(OP_ISTORE_2,        new DispatchStrategyFormat0LV(opcodeName(OP_ISTORE_2), 2));
        setCase(OP_LSTORE_2,        new DispatchStrategyFormat0LV(opcodeName(OP_LSTORE_2), 2));
        setCase(OP_ASTORE_3,        new DispatchStrategyFormat0LV(opcodeName(OP_ASTORE_3), 3));
        setCase(OP_DSTORE_3,        new DispatchStrategyFormat0LV(opcodeName(OP_DSTORE_3), 3));
        setCase(OP_FSTORE_3,        new DispatchStrategyFormat0LV(opcodeName(OP_FSTORE_3), 3));
        setCase(OP_ISTORE_3,        new DispatchStrategyFormat0LV(opcodeName(OP_ISTORE_3), 3));
        setCase(OP_LSTORE_3,        new DispatchStrategyFormat0LV(opcodeName(OP_LSTORE_3), 3));
        setCase(OP_BIPUSH,          new DispatchStrategyFormat1SB(opcodeName(OP_BIPUSH)));
        setCase(OP_SIPUSH,          new DispatchStrategyFormat1SW(opcodeName(OP_SIPUSH)));
        setCase(OP_LDC,             new DispatchStrategyFormat1ZUX(opcodeName(OP_LDC), false));
        setCase(OP_LDC_W,           new DispatchStrategyFormat1ZUX(opcodeName(OP_LDC_W), true));
        setCase(OP_LDC2_W,          new DispatchStrategyFormat1ZUX(opcodeName(OP_LDC2_W), true));
        setCase(OP_ACONST_NULL,     new DispatchStrategyFormat0(opcodeName(OP_ACONST_NULL)));
        setCase(OP_DCONST_0,        new DispatchStrategyFormat0(opcodeName(OP_DCONST_0)));
        setCase(OP_DCONST_1,        new DispatchStrategyFormat0(opcodeName(OP_DCONST_1)));
        setCase(OP_FCONST_0,        new DispatchStrategyFormat0(opcodeName(OP_FCONST_0)));
        setCase(OP_FCONST_1,        new DispatchStrategyFormat0(opcodeName(OP_FCONST_1)));
        setCase(OP_FCONST_2,        new DispatchStrategyFormat0(opcodeName(OP_FCONST_2)));
        setCase(OP_ICONST_M1,       new DispatchStrategyFormat0(opcodeName(OP_ICONST_M1)));
        setCase(OP_ICONST_0,        new DispatchStrategyFormat0(opcodeName(OP_ICONST_0)));
        setCase(OP_ICONST_1,        new DispatchStrategyFormat0(opcodeName(OP_ICONST_1)));
        setCase(OP_ICONST_2,        new DispatchStrategyFormat0(opcodeName(OP_ICONST_2)));
        setCase(OP_ICONST_3,        new DispatchStrategyFormat0(opcodeName(OP_ICONST_3)));
        setCase(OP_ICONST_4,        new DispatchStrategyFormat0(opcodeName(OP_ICONST_4)));
        setCase(OP_ICONST_5,        new DispatchStrategyFormat0(opcodeName(OP_ICONST_5)));
        setCase(OP_LCONST_0,        new DispatchStrategyFormat0(opcodeName(OP_LCONST_0)));
        setCase(OP_LCONST_1,        new DispatchStrategyFormat0(opcodeName(OP_LCONST_1)));
        setCase(OP_DADD,            new DispatchStrategyFormat0(opcodeName(OP_DADD)));
        setCase(OP_FADD,            new DispatchStrategyFormat0(opcodeName(OP_FADD)));
        setCase(OP_IADD,            new DispatchStrategyFormat0(opcodeName(OP_IADD)));
        setCase(OP_LADD,            new DispatchStrategyFormat0(opcodeName(OP_LADD)));
        setCase(OP_DSUB,            new DispatchStrategyFormat0(opcodeName(OP_DSUB)));
        setCase(OP_FSUB,            new DispatchStrategyFormat0(opcodeName(OP_FSUB)));
        setCase(OP_ISUB,            new DispatchStrategyFormat0(opcodeName(OP_ISUB)));
        setCase(OP_LSUB,            new DispatchStrategyFormat0(opcodeName(OP_LSUB)));
        setCase(OP_DMUL,            new DispatchStrategyFormat0(opcodeName(OP_DMUL)));
        setCase(OP_FMUL,            new DispatchStrategyFormat0(opcodeName(OP_FMUL)));
        setCase(OP_IMUL,            new DispatchStrategyFormat0(opcodeName(OP_IMUL)));
        setCase(OP_LMUL,            new DispatchStrategyFormat0(opcodeName(OP_LMUL)));
        setCase(OP_DDIV,            new DispatchStrategyFormat0(opcodeName(OP_DDIV)));
        setCase(OP_FDIV,            new DispatchStrategyFormat0(opcodeName(OP_FDIV)));
        setCase(OP_IDIV,            new DispatchStrategyFormat0(opcodeName(OP_IDIV)));
        setCase(OP_LDIV,            new DispatchStrategyFormat0(opcodeName(OP_LDIV)));
        setCase(OP_DREM,            new DispatchStrategyFormat0(opcodeName(OP_DREM)));
        setCase(OP_FREM,            new DispatchStrategyFormat0(opcodeName(OP_FREM)));
        setCase(OP_IREM,            new DispatchStrategyFormat0(opcodeName(OP_IREM)));
        setCase(OP_LREM,            new DispatchStrategyFormat0(opcodeName(OP_LREM)));
        setCase(OP_DNEG,            new DispatchStrategyFormat0(opcodeName(OP_DNEG)));
        setCase(OP_FNEG,            new DispatchStrategyFormat0(opcodeName(OP_FNEG)));
        setCase(OP_INEG,            new DispatchStrategyFormat0(opcodeName(OP_INEG)));
        setCase(OP_LNEG,            new DispatchStrategyFormat0(opcodeName(OP_LNEG)));
        setCase(OP_ISHL,            new DispatchStrategyFormat0(opcodeName(OP_ISHL)));
        setCase(OP_LSHL,            new DispatchStrategyFormat0(opcodeName(OP_LSHL)));
        setCase(OP_ISHR,            new DispatchStrategyFormat0(opcodeName(OP_ISHR)));
        setCase(OP_LSHR,            new DispatchStrategyFormat0(opcodeName(OP_LSHR)));
        setCase(OP_IUSHR,           new DispatchStrategyFormat0(opcodeName(OP_IUSHR)));
        setCase(OP_LUSHR,           new DispatchStrategyFormat0(opcodeName(OP_LUSHR)));
        setCase(OP_IOR,             new DispatchStrategyFormat0(opcodeName(OP_IOR)));
        setCase(OP_LOR,             new DispatchStrategyFormat0(opcodeName(OP_LOR)));
        setCase(OP_IAND,            new DispatchStrategyFormat0(opcodeName(OP_IAND)));
        setCase(OP_LAND,            new DispatchStrategyFormat0(opcodeName(OP_LAND)));
        setCase(OP_IXOR,            new DispatchStrategyFormat0(opcodeName(OP_IXOR)));
        setCase(OP_LXOR,            new DispatchStrategyFormat0(opcodeName(OP_LXOR)));
        setCase(OP_IINC,            new DispatchStrategyFormat2LVSX(opcodeName(OP_IINC)));
        setCase(OP_DCMPG,           new DispatchStrategyFormat0(opcodeName(OP_DCMPG)));
        setCase(OP_DCMPL,           new DispatchStrategyFormat0(opcodeName(OP_DCMPL)));
        setCase(OP_FCMPG,           new DispatchStrategyFormat0(opcodeName(OP_FCMPG)));
        setCase(OP_FCMPL,           new DispatchStrategyFormat0(opcodeName(OP_FCMPL)));
        setCase(OP_LCMP,            new DispatchStrategyFormat0(opcodeName(OP_LCMP)));
        setCase(OP_I2D,             new DispatchStrategyFormat0(opcodeName(OP_I2D)));
        setCase(OP_I2F,             new DispatchStrategyFormat0(opcodeName(OP_I2F)));
        setCase(OP_I2L,             new DispatchStrategyFormat0(opcodeName(OP_I2L)));
        setCase(OP_L2D,             new DispatchStrategyFormat0(opcodeName(OP_L2D)));
        setCase(OP_L2F,             new DispatchStrategyFormat0(opcodeName(OP_L2F)));
        setCase(OP_F2D,             new DispatchStrategyFormat0(opcodeName(OP_F2D)));
        setCase(OP_D2F,             new DispatchStrategyFormat0(opcodeName(OP_D2F)));
        setCase(OP_D2I,             new DispatchStrategyFormat0(opcodeName(OP_D2I)));
        setCase(OP_D2L,             new DispatchStrategyFormat0(opcodeName(OP_D2L)));
        setCase(OP_F2I,             new DispatchStrategyFormat0(opcodeName(OP_F2I)));
        setCase(OP_F2L,             new DispatchStrategyFormat0(opcodeName(OP_F2L)));
        setCase(OP_I2B,             new DispatchStrategyFormat0(opcodeName(OP_I2B)));
        setCase(OP_I2C,             new DispatchStrategyFormat0(opcodeName(OP_I2C)));
        setCase(OP_I2S,             new DispatchStrategyFormat0(opcodeName(OP_I2S)));
        setCase(OP_L2I,             new DispatchStrategyFormat0(opcodeName(OP_L2I)));
        setCase(OP_NEW,             new DispatchStrategyFormat1CL(opcodeName(OP_NEW)));
        setCase(OP_NEWARRAY,        new DispatchStrategyFormat1AT(opcodeName(OP_NEWARRAY)));
        setCase(OP_ANEWARRAY,       new DispatchStrategyFormat1CL(opcodeName(OP_ANEWARRAY)));
        setCase(OP_MULTIANEWARRAY,  new DispatchStrategyFormat2CLUB(opcodeName(OP_MULTIANEWARRAY)));
        setCase(OP_GETFIELD,        new DispatchStrategyFormat1FI(opcodeName(OP_GETFIELD)));
        setCase(OP_PUTFIELD,        new DispatchStrategyFormat1FI(opcodeName(OP_PUTFIELD)));
        setCase(OP_GETSTATIC,       new DispatchStrategyFormat1FI(opcodeName(OP_GETSTATIC)));
        setCase(OP_PUTSTATIC,       new DispatchStrategyFormat1FI(opcodeName(OP_PUTSTATIC)));
        setCase(OP_AALOAD,          new DispatchStrategyFormat0(opcodeName(OP_AALOAD)));
        setCase(OP_CALOAD,          new DispatchStrategyFormat0(opcodeName(OP_CALOAD)));
        setCase(OP_BALOAD,          new DispatchStrategyFormat0(opcodeName(OP_BALOAD)));
        setCase(OP_DALOAD,          new DispatchStrategyFormat0(opcodeName(OP_DALOAD)));
        setCase(OP_FALOAD,          new DispatchStrategyFormat0(opcodeName(OP_FALOAD)));
        setCase(OP_IALOAD,          new DispatchStrategyFormat0(opcodeName(OP_IALOAD)));
        setCase(OP_LALOAD,          new DispatchStrategyFormat0(opcodeName(OP_LALOAD)));
        setCase(OP_SALOAD,          new DispatchStrategyFormat0(opcodeName(OP_SALOAD)));
        setCase(OP_AASTORE,         new DispatchStrategyFormat0(opcodeName(OP_AASTORE)));
        setCase(OP_CASTORE,         new DispatchStrategyFormat0(opcodeName(OP_CASTORE)));
        setCase(OP_BASTORE,         new DispatchStrategyFormat0(opcodeName(OP_BASTORE)));
        setCase(OP_DASTORE,         new DispatchStrategyFormat0(opcodeName(OP_DASTORE)));
        setCase(OP_FASTORE,         new DispatchStrategyFormat0(opcodeName(OP_FASTORE)));
        setCase(OP_IASTORE,         new DispatchStrategyFormat0(opcodeName(OP_IASTORE)));
        setCase(OP_LASTORE,         new DispatchStrategyFormat0(opcodeName(OP_LASTORE)));
        setCase(OP_SASTORE,         new DispatchStrategyFormat0(opcodeName(OP_SASTORE)));
        setCase(OP_ARRAYLENGTH,     new DispatchStrategyFormat0(opcodeName(OP_ARRAYLENGTH)));
        setCase(OP_INSTANCEOF,      new DispatchStrategyFormat1CL(opcodeName(OP_INSTANCEOF)));
        setCase(OP_CHECKCAST,       new DispatchStrategyFormat1CL(opcodeName(OP_CHECKCAST)));
        setCase(OP_POP,             new DispatchStrategyFormat0(opcodeName(OP_POP)));
        setCase(OP_POP2,            new DispatchStrategyFormat0(opcodeName(OP_POP2)));
        setCase(OP_DUP,             new DispatchStrategyFormat0(opcodeName(OP_DUP)));
        setCase(OP_DUP2,            new DispatchStrategyFormat0(opcodeName(OP_DUP2)));
        setCase(OP_DUP_X1,          new DispatchStrategyFormat0(opcodeName(OP_DUP_X1)));
        setCase(OP_DUP2_X1,         new DispatchStrategyFormat0(opcodeName(OP_DUP2_X1)));
        setCase(OP_DUP_X2,          new DispatchStrategyFormat0(opcodeName(OP_DUP_X2)));
        setCase(OP_DUP2_X2,         new DispatchStrategyFormat0(opcodeName(OP_DUP2_X2)));
        setCase(OP_SWAP,            new DispatchStrategyFormat0(opcodeName(OP_SWAP)));
        setCase(OP_IFEQ,            new DispatchStrategyFormat1ON(opcodeName(OP_IFEQ)));
        setCase(OP_IFGE,            new DispatchStrategyFormat1ON(opcodeName(OP_IFGE)));
        setCase(OP_IFGT,            new DispatchStrategyFormat1ON(opcodeName(OP_IFGT)));
        setCase(OP_IFLE,            new DispatchStrategyFormat1ON(opcodeName(OP_IFLE)));
        setCase(OP_IFLT,            new DispatchStrategyFormat1ON(opcodeName(OP_IFLT)));
        setCase(OP_IFNE,            new DispatchStrategyFormat1ON(opcodeName(OP_IFNE)));
        setCase(OP_IFNONNULL,       new DispatchStrategyFormat1ON(opcodeName(OP_IFNONNULL)));
        setCase(OP_IFNULL,          new DispatchStrategyFormat1ON(opcodeName(OP_IFNULL)));
        setCase(OP_IF_ICMPEQ,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ICMPEQ)));
        setCase(OP_IF_ICMPGE,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ICMPGE)));
        setCase(OP_IF_ICMPGT,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ICMPGT)));
        setCase(OP_IF_ICMPLE,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ICMPLE)));
        setCase(OP_IF_ICMPLT,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ICMPLT)));
        setCase(OP_IF_ICMPNE,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ICMPNE)));
        setCase(OP_IF_ACMPEQ,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ACMPEQ)));
        setCase(OP_IF_ACMPNE,       new DispatchStrategyFormat1ON(opcodeName(OP_IF_ACMPNE)));
        setCase(OP_TABLESWITCH,     new DispatchStrategyFormat1ZSWITCH(opcodeName(OP_TABLESWITCH), true));
        setCase(OP_LOOKUPSWITCH,    new DispatchStrategyFormat1ZSWITCH(opcodeName(OP_LOOKUPSWITCH), false));
        setCase(OP_GOTO,            new DispatchStrategyFormat1ON(opcodeName(OP_GOTO)));
        setCase(OP_GOTO_W,          new DispatchStrategyFormat1OF(opcodeName(OP_GOTO_W)));
        setCase(OP_JSR,             new DispatchStrategyFormat1ON(opcodeName(OP_JSR)));
        setCase(OP_JSR_W,           new DispatchStrategyFormat1OF(opcodeName(OP_JSR_W)));
        setCase(OP_RET,             new DispatchStrategyFormat1LV(opcodeName(OP_RET)));
        setCase(OP_INVOKEINTERFACE, new DispatchStrategyFormat1ME(opcodeName(OP_INVOKEINTERFACE), true));
        setCase(OP_INVOKEVIRTUAL,   new DispatchStrategyFormat1ME(opcodeName(OP_INVOKEVIRTUAL), false));
        setCase(OP_INVOKESPECIAL,   new DispatchStrategyFormat1ME(opcodeName(OP_INVOKESPECIAL), false));
        setCase(OP_INVOKESTATIC,    new DispatchStrategyFormat1ME(opcodeName(OP_INVOKESTATIC), false));
        setCase(OP_INVOKEDYNAMIC,   new DispatchStrategyFormat1CS(opcodeName(OP_INVOKEDYNAMIC)));
        setCase(OP_RETURN,          new DispatchStrategyFormat0(opcodeName(OP_RETURN)));
        setCase(OP_ARETURN,         new DispatchStrategyFormat0(opcodeName(OP_ARETURN)));
        setCase(OP_DRETURN,         new DispatchStrategyFormat0(opcodeName(OP_DRETURN)));
        setCase(OP_FRETURN,         new DispatchStrategyFormat0(opcodeName(OP_FRETURN)));
        setCase(OP_IRETURN,         new DispatchStrategyFormat0(opcodeName(OP_IRETURN)));
        setCase(OP_LRETURN,         new DispatchStrategyFormat0(opcodeName(OP_LRETURN)));
        setCase(OP_ATHROW,          new DispatchStrategyFormat0(opcodeName(OP_ATHROW)));
        setCase(OP_WIDE,            new DispatchStrategyFormat0(opcodeName(OP_WIDE)));
        setCase(OP_MONITORENTER,    new DispatchStrategyFormat0(opcodeName(OP_MONITORENTER)));
        setCase(OP_MONITOREXIT,     new DispatchStrategyFormat0(opcodeName(OP_MONITOREXIT)));
        setCase(OP_BREAKPOINT,      new DispatchStrategyFormat0(opcodeName(OP_BREAKPOINT)));
        setCase(OP_INVOKEHANDLE,    new DispatchStrategyFormat1ME(opcodeName(OP_INVOKEHANDLE), false));
        setCase(OP_IMPDEP1,         new DispatchStrategyFormat0(opcodeName(OP_IMPDEP1)));
        setCase(OP_IMPDEP2,         new DispatchStrategyFormat0(opcodeName(OP_IMPDEP2)));
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
