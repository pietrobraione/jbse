package jbse.algo;

import jbse.mem.State;

public final class BytecodeData_1FI extends BytecodeData {
    @Override
    protected void readImmediates(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        readFieldSignature(state, immediateUnsignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1FI() {
        //nothing to do
    }    
    
    public static BytecodeData_1FI get() {
        return new BytecodeData_1FI();
    }
}
