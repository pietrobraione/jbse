package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicits, 
 * one immediate (field signature).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1FI extends BytecodeData {
    @Override
    protected void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateUnsignedWord(state, calc, 1);
        readFieldSignature(state, calc, immediateUnsignedWord());
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
