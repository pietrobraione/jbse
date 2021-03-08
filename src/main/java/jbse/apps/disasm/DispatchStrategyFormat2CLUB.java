package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 2 operands, the first with type unsigned word (16 bits) 
 * whose meaning is a class/array/interface signature in the constant pool, the second
 * with type unsigned byte (8 bits).
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat2CLUB implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat2CLUB(String text) {
        this.text = text;
    }

    public BytecodeDisassembler doIt() {
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
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}