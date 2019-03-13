package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicits, 
 * one immediate (signed word).
 * 
 * @author Pietro Braione
 */

public final class BytecodeData_1SW extends BytecodeData {
    @Override
    protected void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateSignedWord(state, calc, 1);
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1SW() {
        //nothing to do
    }    

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1SW}.
     */
    public static BytecodeData_1SW get() {
        return new BytecodeData_1SW();
    }
}
