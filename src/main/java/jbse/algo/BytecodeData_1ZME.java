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
    public enum Kind { NONINTERFACE, INTERFACE, BOTH; 
        public static Kind kind(boolean isInterface, boolean isSpecial, boolean isStatic) {
            return (isInterface ? Kind.INTERFACE : (isSpecial || isStatic) ? Kind.BOTH : Kind.NONINTERFACE);
        }
    }
    private final Kind kind;

    @Override
    protected void readImmediates(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        switch (kind) {
        case NONINTERFACE:
            readNoninterfaceMethodSignature(state, immediateUnsignedWord());
            break;
        case INTERFACE:
            readInterfaceMethodSignature(state, immediateUnsignedWord());
            break;
        case BOTH:
            readMethodSignature(state, immediateUnsignedWord());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1ZME(Kind kind) {
        this.kind = kind;
    }    

    /**
     * Factory (with fluent interface).
     * 
     * @param kind a {@link Kind}, whether the method should be find
     *        among the noninterface methods, interface methods, or both.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_1ZME}{@code >},
     *         the actual factory for {@link BytecodeData_1ZME} objects.
     */
    public static Supplier<BytecodeData_1ZME> withInterfaceMethod(Kind kind) {
        return () -> new BytecodeData_1ZME(kind);
    }
}
