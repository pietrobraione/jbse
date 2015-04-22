package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.algo.exc.InterruptException;
import jbse.common.Util;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

public class StrategyRead_1LV {
    private int slot;
    private String varName;
    private Value varValue;
    
    public StrategyRead_1LV() { }
    
    public void doRead(State state) 
    throws ThreadStackEmptyException, InterruptException {
        final Frame frame = state.getCurrentFrame();
        final boolean wide = state.nextWide();
        try {
            if (wide) {
                this.slot = Util.byteCat(frame.getInstruction(1), frame.getInstruction(2));
            } else {
                this.slot = frame.getInstruction(1);
            }
            this.varName = frame.getLocalVariableName(this.slot);
            this.varValue = state.getLocalVariableValue(this.slot);
        } catch (InvalidProgramCounterException | InvalidSlotException e) {
            throwVerifyError(state);
            throw InterruptException.getInstance();
        }
    }
}
