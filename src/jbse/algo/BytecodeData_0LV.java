package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

public final class BytecodeData_0LV extends BytecodeData {
    private final int numVarSlot;
    
    @Override
    protected void readImmediates(State state) throws InterruptException {
        readLocalVariable(state, this.numVarSlot);
    }
    
    /**
     * Do not instantiate!
     */
    private BytecodeData_0LV(int numVarSlot) {
        this.numVarSlot = numVarSlot;
    }    
    
    public static Supplier<BytecodeData_0LV> withVarSlot(int numVarSlot) {
        return () -> new BytecodeData_0LV(numVarSlot);
    }
}
