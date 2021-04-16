package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.mem.Array;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) whose
 * meaning is a primitive type in newarray coding.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1AT implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat1AT(String text) {
        this.text = text;
    }

    public BytecodeDisassembler doIt() {
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
                    retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
                } else {
                    retVal += type + " [" + UB + "]";
                }
            } catch (InvalidProgramCounterException e) {
                //unrecognized bytecode
                retVal = DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}