package jbse.algo;

import jbse.mem.State;

/**
 * One immediate parameter (offset, near).
 * 
 * @author Pietro Braione
 *
 */
public final class BytecodeData_1ON extends BytecodeData {
    @Override
    public void readImmediates(State state) throws InterruptException {
        readImmediateSignedWord(state, 1);
        readJump(state, immediateSignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1ON() {
        //nothing to do
    }
    
    public static BytecodeData_1ON get() {
        return new BytecodeData_1ON();
    }
}
