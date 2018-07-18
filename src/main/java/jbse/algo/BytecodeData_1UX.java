package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;

/**
 * Zero implicits, 
 * one immediate (unsigned byte or word).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1UX extends BytecodeData {
    @Override
    public void readImmediates(State state) 
    throws InterruptException, ClasspathException, FrozenStateException {
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
