package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.common.Util;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type signed word (16 bits) whose meaning 
 * is a (near) jump offset.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1ON implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat1ON(String text) { 
        this.text = text; 
    }

    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> {
            String retVal = DispatchStrategyFormat1ON.this.text + " ";
            try {
                final short SW = Util.byteCatShort(f.getInstruction(1), f.getInstruction(2));
                final int target = f.getProgramCounter() + SW;
                retVal += target;
            } catch (InvalidProgramCounterException e) {
                //unrecognized bytecode
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}