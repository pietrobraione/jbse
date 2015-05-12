package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_2CLUB extends BytecodeData {
    @Override
    protected void read(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        readClassName(state, immediateUnsignedWord());
        readImmediateUnsignedByte(state, 3);
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_2CLUB() {
        //nothing to do
    }    
    
    public static BytecodeData_2CLUB get() {
        return new BytecodeData_2CLUB();
    }
}
