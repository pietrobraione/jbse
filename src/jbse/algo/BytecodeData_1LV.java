package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_1LV extends BytecodeData {
    @Override
    public void read(State state) throws InterruptException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, 1);
            readLocalVariable(state, immediateUnsignedWord());
        } else {
            readImmediateUnsignedByte(state, 1);
            readLocalVariable(state, immediateUnsignedByte());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1LV() {
        //nothing to do
    }    
    
    public static BytecodeData_1LV get() {
        return new BytecodeData_1LV();
    }
}
