package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.NEWARRAY_OFFSET;

import java.util.function.Supplier;

import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
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
    protected void preCook(State state) 
    throws InterruptException, InvalidInputException, ClasspathException, RenameUnsupportedException {
        //sets the array length
        try {
            this.dimensionsCounts = new Primitive[] { (Primitive) this.data.operand(0) };
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }

        //sets the array type
        try {
            this.arrayType = state.getClassHierarchy().loadCreateClass("" + Type.ARRAYOF + this.data.primitiveType());
        } catch (ClassFileNotFoundException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | BadClassFileVersionException | 
                 WrongClassNameException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> NEWARRAY_OFFSET;
    }
}
