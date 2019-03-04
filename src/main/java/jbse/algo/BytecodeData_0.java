package jbse.algo;

import jbse.mem.State;
import jbse.val.Calculator;

/**
 * Zero implicits, zero immediates.
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_0 extends BytecodeData {
    @Override
    protected void readImmediates(State state, Calculator calc) throws InterruptException {
        //nothing to do
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_0() {
        //nothing to do
    }

    public static BytecodeData_0 get() {
        return new BytecodeData_0();
    }
}
