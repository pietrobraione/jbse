package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.common.Util;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type signed dword (32 bits) 
 * whose meaning is a (far) jump offset.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1OF implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat1OF(String text) {
        this.text = text;
    }

    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> {
            String retVal = DispatchStrategyFormat1OF.this.text + " ";
            try {
                final int SD = Util.byteCat(f.getInstruction(1), f.getInstruction(2), f.getInstruction(3), f.getInstruction(4));
                final int target = f.getProgramCounter() + SD;
                retVal += target;
            } catch (InvalidProgramCounterException e) {
                //unrecognized bytecode
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}