package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

public final class BytecodeData_SWITCH extends BytecodeData {
    final boolean isTableSwitch;
    
    @Override
    public void readImmediates(State state) throws InterruptException {
        try {
            setSwitchTable(new SwitchTable(state.getCurrentFrame(), state.getCalculator(), this.isTableSwitch));
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            failExecution(e);
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_SWITCH(boolean isTableSwitch) {
        this.isTableSwitch = isTableSwitch;
    }
    
    public static Supplier<BytecodeData_SWITCH> whereTableSwitch(boolean isTableSwitch) {
        return () -> new BytecodeData_SWITCH(isTableSwitch);
    }
}
