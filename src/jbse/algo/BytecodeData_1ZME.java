package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

/**
 * One implicit (boolean, is interface method?), 
 * one immediate (method signature).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1ZME extends BytecodeData {
    private final boolean isInterfaceMethod;
    
    @Override
    protected void readImmediates(State state) throws InterruptException {
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
    private BytecodeData_1ZME(boolean isInterfaceMethod) {
        this.isInterfaceMethod = isInterfaceMethod;
    }    
    
    /**
     * Factory (with fluent interface).
     * 
     * @param isInterfaceMethod a {@code boolean}, whether the
     *        method is interface or noninterface.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_1ZME}{@code >},
     *         the actual factory for {@link BytecodeData_1ZME} objects.
     */
    public static Supplier<BytecodeData_1ZME> withInterfaceMethod(boolean isInterfaceMethod) {
        return () -> new BytecodeData_1ZME(isInterfaceMethod);
    }
}
