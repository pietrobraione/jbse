package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

public final class BytecodeData_1ZIM extends BytecodeData {
    final boolean wide;
    
    @Override
    public void readImmediates(State state) throws InterruptException {
        if (this.wide) {
            readImmediateUnsignedWord(state, 1);
        } else {
            readImmediateUnsignedByte(state, 1);
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1ZIM(boolean wide) {
        this.wide = wide;
    }    
    
    public static Supplier<BytecodeData_1ZIM> withWide(boolean wide) {
        return () -> new BytecodeData_1ZIM(wide);
    }
}
