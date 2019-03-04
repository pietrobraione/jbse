package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicits, 
 * one immediate (local variable).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_1LV extends BytecodeData {
    @Override
    public void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, calc, 1);
            readLocalVariable(state, calc, immediateUnsignedWord());
        } else {
            readImmediateUnsignedByte(state, calc, 1);
            readLocalVariable(state, calc, immediateUnsignedByte());
        }
    }

    /**
     * Do not instantiate!
     */
    private BytecodeData_1LV() {
        //nothing to do
    }

    /**
     * Factory method.
     * 
     * @return a {@link BytecodeData_1LV}.
     */
    public static BytecodeData_1LV get() {
        return new BytecodeData_1LV();
    }
}
