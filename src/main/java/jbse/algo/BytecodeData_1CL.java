package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicits, 
 * one immediate (class name).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1CL extends BytecodeData {
    @Override
    protected void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateUnsignedWord(state, calc, 1);
        readClassName(state, calc, immediateUnsignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1CL() {
        //nothing to do
    }    

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1CL}.
     */
    public static BytecodeData_1CL get() {
        return new BytecodeData_1CL();
    }
}
