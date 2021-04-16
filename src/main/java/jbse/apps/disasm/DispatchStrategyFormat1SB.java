package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type signed byte (8 bits).
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1SB implements DispatchStrategyFormat {
    private final String text;
    public DispatchStrategyFormat1SB(String text) { this.text = text; }
    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> { 
            String retVal = DispatchStrategyFormat1SB.this.text + " ";
            try {
                final byte SB = f.getInstruction(1);
                retVal += SB;
            } catch (InvalidProgramCounterException e) {
                //unrecognized bytecode
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;					
        };
    }		
}