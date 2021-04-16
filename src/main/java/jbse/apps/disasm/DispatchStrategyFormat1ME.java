package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
 * meaning is a method signature in the constant pool.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1ME implements DispatchStrategyFormat {
    private final String text;
    private final boolean isInterface;

    public DispatchStrategyFormat1ME(String text, boolean isInterface) { 
        this.text = text; 
        this.isInterface = isInterface;
    }

    public BytecodeDisassembler doIt() {
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
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}