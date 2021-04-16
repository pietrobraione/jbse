package jbse.apps.disasm;

import static jbse.common.Util.asUnsignedByte;

import jbse.bc.ClassHierarchy;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) 
 * or unsigned word (16 bits) whose meaning is a literal constant in the constant pool.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1ZUX implements DispatchStrategyFormat {
    private final String text;
    private final boolean wide;

    public DispatchStrategyFormat1ZUX(String text, boolean wide) { 
        this.text = text; 
        this.wide = wide;
    }

    public BytecodeDisassembler doIt() {
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
                    retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
                }
            } catch (InvalidProgramCounterException | InvalidIndexException |
                     ClassFileIllFormedException | UnexpectedInternalException e) {
                //unrecognized bytecode
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }

            return retVal;
        };
    }		
}