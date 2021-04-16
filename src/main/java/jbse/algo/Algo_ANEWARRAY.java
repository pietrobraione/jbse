package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.bc.Offsets.ANEWARRAY_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.isArray;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;

/**
 * Algorithm managing the anewarray bytecode.
 * 
 * @author Pietro Braione
 */
final class Algo_ANEWARRAY extends Algo_XNEWARRAY<BytecodeData_1CL> {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_1CL> bytecodeData() {
        return BytecodeData_1CL::get;
    }

    @Override
    protected void preCook(State state) throws InterruptException, ClasspathException, ThreadStackEmptyException, InvalidInputException {
        //sets the array length
        try {
            this.dimensionsCounts = new Primitive[] { (Primitive) this.data.operand(0) };
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }


        try {
            //resolves the class
            //TODO the JVMS v8, anewarray bytecode, prescribes to resolve the member class; It is not clear what initiating loader should be assumed for the array class. We assume the defining loader of the current class, so we can directly resolve the name of the array class.
            final ClassFile currentClass = state.getCurrentClass();
            final boolean memberIsArray = isArray(this.data.className());
            this.arrayType = state.getClassHierarchy().resolveClass(currentClass, "" + ARRAYOF + (memberIsArray ? "" : REFERENCE) + this.data.className() + (memberIsArray ? "" : TYPEEND), state.bypassStandardLoading());
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            //TODO this exception should wrap a ClassNotFoundException
            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
            exitFromAlgorithm();
        } catch (BadClassFileVersionException e) {
            throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            //TODO throw LinkageError instead
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (RenameUnsupportedException e) {
        	//this should never happen
        	failExecution(e);
        }
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> ANEWARRAY_OFFSET;
    }
}