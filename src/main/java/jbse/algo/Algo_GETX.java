package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.GETX_PUTX_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_FIELD_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;

//TODO extract common superclass with Algo_PUTX and eliminate duplicate code
/**
 * Abstract {@link Algorithm} for the get* bytecodes (get[field/static]).
 * It decides over the value loaded to the operand stack in the cases 
 * it is an uninitialized symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
abstract class Algo_GETX extends Algo_XLOAD_GETX<BytecodeData_1FI> {

    protected ClassFile fieldClassResolved; //set by cook

    @Override
    protected final Supplier<BytecodeData_1FI> bytecodeData() {
        return BytecodeData_1FI::get;
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //performs field resolution
            try {
                final ClassFile currentClass = state.getCurrentClass();    
                this.fieldClassResolved = state.getClassHierarchy().resolveField(currentClass, this.data.signature(), state.bypassStandardLoading());
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
            } catch (FieldNotFoundException e) {
                throwNew(state, this.ctx.getCalculator(), NO_SUCH_FIELD_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException | FieldNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //checks the field
            try {
                check(state);
            } catch (FieldNotFoundException e) {
                //this should never happen
                failExecution(e);
            }

            //reads the field value
            final Signature fieldSignatureResolved = new Signature(this.fieldClassResolved.getClassName(), this.data.signature().getDescriptor(), this.data.signature().getName());
            this.valToLoad = source(state).getFieldValue(fieldSignatureResolved);
        };
    }

    protected abstract void check(State state)
    throws ClasspathException, FieldNotFoundException, InterruptException;

    protected abstract Objekt source(State state)
    throws ClasspathException, DecisionException, InterruptException, ContradictionException, FrozenStateException;

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> GETX_PUTX_OFFSET;
    }
}
