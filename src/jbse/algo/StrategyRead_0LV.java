package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.algo.exc.InterruptException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

public class StrategyRead_0LV {
    private final int slot;
    private String varName;
    private Value varValue;
    
    public StrategyRead_0LV(int slot) {
        this.slot = slot;
    }
    
    public void doRead(State state) 
    throws ThreadStackEmptyException, InterruptException {
        final Frame frame = state.getCurrentFrame();
        this.varName = frame.getLocalVariableName(this.slot);
        try {
            this.varValue = state.getLocalVariableValue(this.slot);
        } catch (InvalidSlotException e) {
            throwVerifyError(state);
            throw InterruptException.getInstance();
        }
    }
}
