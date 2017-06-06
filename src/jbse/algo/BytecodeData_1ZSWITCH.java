package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwVerifyError;

import java.util.function.Supplier;

import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * One implicit (boolean, is the switch a tableswitch?),
 * one immediate ({@link SwitchTable}).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1ZSWITCH extends BytecodeData {
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
    private BytecodeData_1ZSWITCH(boolean isTableSwitch) {
        this.isTableSwitch = isTableSwitch;
    }
    
    /**
     * Factory (with fluent interface).
     * 
     * @param isTableSwitch a {@code boolean}, whether the switch
     *        is a tableswitch.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_1ZOF}{@code >},
     *         the actual factory for {@link BytecodeData_1ZOF} objects.
     */
    public static Supplier<BytecodeData_1ZSWITCH> whereTableSwitch(boolean isTableSwitch) {
        return () -> new BytecodeData_1ZSWITCH(isTableSwitch);
    }
}
