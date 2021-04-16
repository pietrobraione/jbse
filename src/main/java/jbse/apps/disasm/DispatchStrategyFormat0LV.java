package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.mem.Frame;

/**
 * A formatter for bytecodes with 0 operands, which returns their name plus a local
 * variable whose index is specified in the constructor.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat0LV implements DispatchStrategyFormat {
    private final String text;
    private final int slot;

    public DispatchStrategyFormat0LV(String text, int slot) { 
        this.text = text; 
        this.slot = slot; 
    }

    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> { 
            final String varName = f.getLocalVariableDeclaredName(DispatchStrategyFormat0LV.this.slot);
            return DispatchStrategyFormat0LV.this.text + (varName == null ? "" : " [" + varName + "]"); 
        };
    }		
}