package jbse.algo;

import jbse.mem.State;

/**
 * Zero implicits, 
 * one immediate (local variable).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1LV extends BytecodeData {
    @Override
    public void readImmediates(State state) throws InterruptException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, 1);
            readLocalVariable(state, immediateUnsignedWord());
        } else {
            readImmediateUnsignedByte(state, 1);
            readLocalVariable(state, immediateUnsignedByte());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1LV() {
        //nothing to do
    }

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1LV}.
     */
    public static BytecodeData_1LV get() {
        return new BytecodeData_1LV();
    }
}
