package jbse.algo;

import jbse.mem.State;

/**
 * Zero implicits, 
 * two immediates (local variable, signed byte or word).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_2LVSX extends BytecodeData {
    @Override
    public void readImmediates(State state) throws InterruptException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, 1);
            readLocalVariable(state, immediateUnsignedWord());
            readImmediateSignedWord(state, 3);
        } else {
            readImmediateUnsignedByte(state, 1);
            readLocalVariable(state, immediateUnsignedByte());
            readImmediateSignedByte(state, 2);
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_2LVSX() {
        //nothing to do
    }    
    
    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_2LVSX}.
     */
    public static BytecodeData_2LVSX get() {
        return new BytecodeData_2LVSX();
    }
}
