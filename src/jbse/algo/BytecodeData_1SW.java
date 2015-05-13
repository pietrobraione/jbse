package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_1SW extends BytecodeData {
    @Override
    protected void readImmediates(State state) throws InterruptException {
        readImmediateSignedWord(state, 1);
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1SW() {
        //nothing to do
    }    
    
    public static BytecodeData_1SW get() {
        return new BytecodeData_1SW();
    }
}
