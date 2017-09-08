package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

/**
 * One implicit (boolean, is offset far?), 
 * one immediate (offset).
 * 
 * @author Pietro Braione
 *
 */
public final class BytecodeData_1ZOF extends BytecodeData {
    final boolean far;

    @Override
    public void readImmediates(State state) throws InterruptException {
        if (this.far) {
            readImmediateSignedDword(state, 1);
            readJump(state, immediateSignedDword());
        } else {
            readImmediateSignedWord(state, 1);
            readJump(state, immediateSignedWord());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1ZOF(boolean far) {
        this.far = far;
    }

    /**
     * Factory (with fluent interface).
     * 
     * @param far a {@code boolean}, whether the jump is a far jump.
     *        It is the value of the implicit of the created object.
     * @return a {@link Supplier}{@code <}{@link BytecodeData_1ZOF}{@code >},
     *         the actual factory for {@link BytecodeData_1ZOF} objects.
     */
    public static Supplier<BytecodeData_1ZOF> withFarOffset(boolean far) {
        return () -> new BytecodeData_1ZOF(far);
    }
}
