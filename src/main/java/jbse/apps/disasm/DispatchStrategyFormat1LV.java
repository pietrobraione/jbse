package jbse.apps.disasm;

import static jbse.bc.Opcodes.OP_WIDE;

import jbse.bc.ClassHierarchy;
import jbse.common.Util;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type unsigned byte (8 bits) or unsigned 
 * word (16 bits) whose meaning is the index of a local variable.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1LV implements DispatchStrategyFormat {
    private final String text;
    public DispatchStrategyFormat1LV(String text) { this.text = text; }
    public BytecodeDisassembler doIt() {
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
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}