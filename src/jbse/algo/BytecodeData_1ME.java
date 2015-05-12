package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

public final class BytecodeData_1ME extends BytecodeData {
    private final boolean isInterfaceMethod;
    
    @Override
    protected void read(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        if (this.isInterfaceMethod) {
            readInterfaceMethodSignature(state, immediateUnsignedWord());
        } else {
            readNoninterfaceMethodSignature(state, immediateUnsignedWord());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1ME(boolean isInterfaceMethod) {
        this.isInterfaceMethod = isInterfaceMethod;
    }    
    
    public static Supplier<BytecodeData_1ME> withInterfaceMethod(boolean isInterfaceMethod) {
        return () -> new BytecodeData_1ME(isInterfaceMethod);
    }
}
