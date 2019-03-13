package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicit, 
 * one immediate (offset, near).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1ON extends BytecodeData {
    @Override
    public void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateSignedWord(state, calc, 1);
        readJump(state, immediateSignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1ON() {
        //nothing to do
    }

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1ON}.
     */
    public static BytecodeData_1ON get() {
        return new BytecodeData_1ON();
    }
}
