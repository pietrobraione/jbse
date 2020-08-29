package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicits, 
 * one immediate (call site specifier).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1CS extends BytecodeData {
    @Override
    protected void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        readImmediateUnsignedWord(state, calc, 1);
        readCallSiteSpecifier(state, calc, immediateUnsignedWord());
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1CS() {
    	//nothing to do
    }    

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1CS}.
     */
    public static BytecodeData_1CS get() {
        return new BytecodeData_1CS();
    }
}
