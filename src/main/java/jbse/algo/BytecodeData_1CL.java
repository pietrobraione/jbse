package jbse.algo;

import jbse.mem.State;

/**
 * Zero implicits, 
 * one immediate (class name).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1CL extends BytecodeData {
    @Override
    protected void readImmediates(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        readClassName(state, immediateUnsignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1CL() {
        //nothing to do
    }    

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1CL}.
     */
    public static BytecodeData_1CL get() {
        return new BytecodeData_1CL();
    }
}
