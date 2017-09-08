package jbse.algo;

import jbse.mem.State;

/**
 * Zero implicits, 
 * one immediate (field signature).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1FI extends BytecodeData {
    @Override
    protected void readImmediates(State state) throws InterruptException {
        readImmediateUnsignedWord(state, 1);
        readFieldSignature(state, immediateUnsignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1FI() {
        //nothing to do
    }    

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1FI}.
     */
    public static BytecodeData_1FI get() {
        return new BytecodeData_1FI();
    }
}
