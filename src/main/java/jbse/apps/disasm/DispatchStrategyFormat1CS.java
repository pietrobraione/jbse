package jbse.apps.disasm;

import jbse.bc.CallSiteSpecifier;
import jbse.bc.ClassHierarchy;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Util;
import jbse.mem.Frame;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for bytecodes with 1 operand with type unsigned word (16 bits) whose
 * meaning is a call site specifier in the constant pool.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat1CS implements DispatchStrategyFormat {
    private final String text;
    public DispatchStrategyFormat1CS(String text) { this.text = text; }
    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> { 
            String retVal = DispatchStrategyFormat1CS.this.text + " ";
            try {
                final int UW = Util.byteCat(f.getInstruction(1), f.getInstruction(2));
                final CallSiteSpecifier css = f.getMethodClass().getCallSiteSpecifier(UW);
                retVal += " " + css.getDescriptor() + ":" + css.getName() + " * " + css.getBootstrapMethodSignature().toString(); 
            } catch (InvalidProgramCounterException | InvalidIndexException | 
            		ClassFileIllFormedException e) {
                //unrecognized bytecode
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;					
        };
    }		
}