package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

/**
 * One implicit (boolean, is wide?),
 * one immediate (unsigned byte or word).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1ZUX extends BytecodeData {
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
    private BytecodeData_1ZUX(boolean wide) {
        this.wide = wide;
    }    

    /**
     * Factory (with fluent interface).
     * 
     * @param wide a {@code boolean}, whether the
     *        bytecode is its wide variant or not.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_1ZUX}{@code >},
     *         the actual factory for {@link BytecodeData_1ZUX} objects.
     */
    public static Supplier<BytecodeData_1ZUX> withWide(boolean wide) {
        return () -> new BytecodeData_1ZUX(wide);
    }
}
