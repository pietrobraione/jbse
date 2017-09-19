package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.NEWARRAY_OFFSET;

import java.util.function.Supplier;

import jbse.common.Type;
import jbse.mem.State;
import jbse.val.Primitive;

/**
 * Algorithm managing the newarray bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_NEWARRAY extends Algo_XNEWARRAY<BytecodeData_1AT> {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_1AT> bytecodeData() {
        return BytecodeData_1AT::get;
    }

    @Override
    protected void preCook(State state) throws InterruptException {
        //sets the array length
        try {
            this.dimensionsCounts = new Primitive[] { (Primitive) this.data.operand(0) };
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

        //sets the array type
        this.arrayType = "" + Type.ARRAYOF + this.data.primitiveType();
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> NEWARRAY_OFFSET;
    }
}
