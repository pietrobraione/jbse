package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_2LVIM extends BytecodeData {
    @Override
    public void readImmediates(State state) throws InterruptException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, 1);
            readLocalVariable(state, immediateUnsignedWord());
            readImmediateUnsignedWord(state, 3);
        } else {
            readImmediateUnsignedByte(state, 1);
            readLocalVariable(state, immediateUnsignedByte());
            readImmediateUnsignedByte(state, 2);
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_2LVIM() {
        //nothing to do
    }    
    
    public static BytecodeData_2LVIM get() {
        return new BytecodeData_2LVIM();
    }
}
