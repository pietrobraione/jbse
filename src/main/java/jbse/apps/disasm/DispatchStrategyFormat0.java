package jbse.apps.disasm;

/**
 * A formatter for bytecodes with 0 operands, which just returns their name.
 * 
 * @author Pietro Braione
 */
class DispatchStrategyFormat0 implements DispatchStrategyFormat {
    private final String text;

    public DispatchStrategyFormat0(String text) { 
        this.text = text;
    }

    public BytecodeDisassembler doIt() {
        return (f, hier) -> DispatchStrategyFormat0.this.text; 
    }
}