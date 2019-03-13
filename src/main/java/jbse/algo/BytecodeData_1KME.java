package jbse.algo;

import java.util.function.Supplier;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * One implicit (kind, where is the method declared?), 
 * one immediate (method signature).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1KME extends BytecodeData {
    public enum Kind { NONINTERFACE, INTERFACE, BOTH; 
        public static Kind kind(boolean isInterface, boolean isSpecial, boolean isStatic) {
            return (isInterface ? Kind.INTERFACE : (isSpecial || isStatic) ? Kind.BOTH : Kind.NONINTERFACE);
        }
    }
    private final Kind kind;

    @Override
    protected void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateUnsignedWord(state, calc, 1);
        switch (kind) {
        case NONINTERFACE:
            readNoninterfaceMethodSignature(state, calc, immediateUnsignedWord());
            break;
        case INTERFACE:
            readInterfaceMethodSignature(state, calc, immediateUnsignedWord());
            break;
        case BOTH:
            readMethodSignature(state, calc, immediateUnsignedWord());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1KME(Kind kind) {
        this.kind = kind;
    }    

    /**
     * Factory (with fluent interface).
     * 
     * @param kind a {@link Kind}, whether the method is declared
     *        among the noninterface methods, interface methods, or either.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_1KME}{@code >},
     *         the actual factory for {@link BytecodeData_1KME} objects.
     */
    public static Supplier<BytecodeData_1KME> withMethod(Kind kind) {
        return () -> new BytecodeData_1KME(kind);
    }
}
