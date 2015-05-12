package jbse.algo;

import static jbse.mem.Array.arrayPrimitiveType;

import jbse.mem.State;

public final class BytecodeData_1AT extends BytecodeData {
    @Override
    protected void read(State state) throws InterruptException {
        readImmediateUnsignedByte(state, 1);
        setPrimitiveType(state, arrayPrimitiveType(immediateUnsignedByte()));
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1AT() {
        //nothing to do
    }    
    
    public static BytecodeData_1AT get() {
        return new BytecodeData_1AT();
    }
}
