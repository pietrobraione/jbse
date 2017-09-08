package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.MULTIANEWARRAY_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;

import java.util.function.Supplier;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;

/**
 * Algorithm managing the multianewarray bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_MULTIANEWARRAY extends Algo_XNEWARRAY<BytecodeData_2CLUB> {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> (int) this.data.immediateUnsignedByte();
    }

    @Override
    protected Supplier<BytecodeData_2CLUB> bytecodeData() {
        return BytecodeData_2CLUB::get;
    }

    @Override
    protected void preCook(State state) throws InterruptException {
        //checks the number of dimensions
        final int ndims = this.data.immediateUnsignedByte();
        if (ndims <= 0) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

        //sets the array dimensions
        this.dimensionsCounts = new Primitive[ndims];
        try {
            for (int i = 0; i < ndims; ++i) {
                this.dimensionsCounts[i] = (Primitive) this.data.operand(i);
                //TODO length check?
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }

        //sets the array type
        this.arrayType = this.data.className();

        //resolves the member class
        try {
            final String currentClassName = state.getCurrentMethodSignature().getClassName();
            state.getClassHierarchy().resolveClass(currentClassName, this.data.className()); //same as resolving the member class
        } catch (ClassFileNotFoundException e) {
            throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> MULTIANEWARRAY_OFFSET;
    }
}