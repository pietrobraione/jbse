package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_1CL extends BytecodeData {
    @Override
    protected void read(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        readClassName(state, immediateUnsignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1CL() {
        //nothing to do
    }    
    
    public static BytecodeData_1CL get() {
        return new BytecodeData_1CL();
    }
}
