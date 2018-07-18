package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;

/**
 * Zero implicits, 
 * two immediates (class name, unsigned byte).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_2CLUB extends BytecodeData {
    @Override
    protected void readImmediates(State state) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateUnsignedWord(state, 1);
        readClassName(state, immediateUnsignedWord());
        readImmediateUnsignedByte(state, 3);
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_2CLUB() {
        //nothing to do
    }

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_2CLUB}.
     */
    public static BytecodeData_2CLUB get() {
        return new BytecodeData_2CLUB();
    }
}
