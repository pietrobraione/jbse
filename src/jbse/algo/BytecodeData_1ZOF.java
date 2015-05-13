package jbse.algo;

import java.util.function.Supplier;

import jbse.mem.State;

/**
 * One implicit parameter (boolean, is offset far?), 
 * one immediate parameter (offset).
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
    
    public static Supplier<BytecodeData_1ZOF> withFarOffset(boolean far) {
        return () -> new BytecodeData_1ZOF(far);
    }
}
