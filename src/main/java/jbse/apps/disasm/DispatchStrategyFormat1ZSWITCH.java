package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.mem.Frame;
import jbse.mem.SwitchTable;
import jbse.mem.exc.InvalidProgramCounterException;

/**
 * A formatter for *switch bytecodes.
 *
 * @author Pietro Braione
 */
class DispatchStrategyFormat1ZSWITCH implements DispatchStrategyFormat {
    private final String text;
    private final boolean isTableSwitch;

    public DispatchStrategyFormat1ZSWITCH(String text, boolean isTableSwitch) {
        this.text = text; this.isTableSwitch = isTableSwitch;
    }

    public BytecodeDisassembler doIt() {
        return (Frame f, ClassHierarchy hier) -> {
            String retVal = DispatchStrategyFormat1ZSWITCH.this.text + " ";
            SwitchTable tab;
            try {
                tab = new SwitchTable(f, DispatchStrategyFormat1ZSWITCH.this.isTableSwitch);
                final StringBuilder buf = new StringBuilder();
                for (int val : tab) {
                    final int target = f.getProgramCounter() + tab.jumpOffset(val);
                    buf.append(val);
                    buf.append(":");
                    buf.append(target);
                    buf.append(" ");
                }
                retVal += buf.toString();
                final int target = f.getProgramCounter() + tab.jumpOffsetDefault();
                retVal += "dflt:" + target;
            } catch (InvalidProgramCounterException e) {
                retVal += DispatchStrategyFormat.UNRECOGNIZED_BYTECODE;
            }
            return retVal;
        };
    }		
}