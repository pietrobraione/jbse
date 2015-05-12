package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_1IM extends BytecodeData {
    @Override
    public void read(State state) throws InterruptException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, 1);
        } else {
            readImmediateUnsignedByte(state, 1);
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1IM() {
        //nothing to do
    }    
    
    public static BytecodeData_1IM get() {
        return new BytecodeData_1IM();
    }
}
