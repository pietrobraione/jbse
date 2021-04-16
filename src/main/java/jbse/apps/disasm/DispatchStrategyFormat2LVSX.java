package jbse.apps.disasm;

import static jbse.bc.Opcodes.OP_WIDE;

import jbse.bc.ClassHierarchy;
import jbse.common.Util;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 2 operand, the first with type unsigned byte (8 bits) or 
 * unsigned word (16 bits) whose meaning is the index of a local variable, the second
 * with type signed byte (8 bits) or signed word (16 bits) whose meaning is an immediate
 * constant.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat2LVSX implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat2LVSX(String text) {
        this.text = text;
    }

    public BytecodeDisassembler doIt() {
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
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}