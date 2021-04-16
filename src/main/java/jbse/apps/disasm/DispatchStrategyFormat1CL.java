package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
 * meaning is a class/array/interface signature in the constant pool.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1CL implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat1CL(String text) {
        this.text = text;
    }

    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> { 
            String retVal = DispatchStrategyFormat1CL.this.text + " ";
            try {
                final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
                final String sig = f.getMethodClass().getClassSignature(UW);
                retVal += sig;
            } catch (InvalidProgramCounterException | InvalidIndexException |
                     UnexpectedInternalException e) {
                //unrecognized bytecode
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}