package jbse.algo;

import jbse.mem.State;

/**
 * Zero implicits, 
 * one immediate (unsigned byte or word).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1UX extends BytecodeData {
    @Override
    public void readImmediates(State state) throws InterruptException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, 1);
        } else {
            readImmediateUnsignedByte(state, 1);
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1UX() {
        //nothing to do
    }    

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1UX}.
     */
    public static BytecodeData_1UX get() {
        return new BytecodeData_1UX();
    }
}
