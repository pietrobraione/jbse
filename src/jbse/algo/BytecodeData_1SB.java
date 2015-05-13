package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_1SB extends BytecodeData {
    @Override
    protected void readImmediates(State state) throws InterruptException {
        readImmediateSignedByte(state, 1);
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1SB() {
        //nothing to do
    }    
    
    public static BytecodeData_1SB get() {
        return new BytecodeData_1SB();
    }
}
