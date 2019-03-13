package jbse.algo;

import jbse.common.exc.ClasspathException;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Calculator;

/**
 * Zero implicits, 
 * two immediates (local variable, signed byte or word).
 * 
 * @author Pietro Braione
 */
public final class BytecodeData_2LVSX extends BytecodeData {
    @Override
    public void readImmediates(State state, Calculator calc) 
    throws InterruptException, ClasspathException, FrozenStateException {
        if (nextWide()) {
            readImmediateUnsignedWord(state, calc, 1);
            readLocalVariable(state, calc, immediateUnsignedWord());
            readImmediateSignedWord(state, calc, 3);
        } else {
            readImmediateUnsignedByte(state, calc, 1);
            readLocalVariable(state, calc, immediateUnsignedByte());
            readImmediateSignedByte(state, calc, 2);
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
