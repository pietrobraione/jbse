package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.val.Reference;

//TODO merge with Algo_GETFIELD
/**
 * {@link Algorithm} managing the "set field in object" 
 * (putfield) bytecode.
 * 
 * @author Pietro Braione
 *
 */
final class Algo_PUTFIELD extends Algo_PUTX {

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void check(State state, String currentClass)
    throws FieldNotFoundException, BadClassFileException, InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();
        final ClassFile fieldClassFile = state.getClassHierarchy().getClassFile(fieldClassName);

        //checks that the field is not static
        if (fieldClassFile.isFieldStatic(this.fieldSignatureResolved)) {
            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }

        //checks that if the field is final is declared in the current class
        if (fieldClassFile.isFieldFinal(this.fieldSignatureResolved) &&
            !fieldClassName.equals(currentClass)) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        }

        //TODO compare types of field and of the value to put in
    }

    @Override
    protected void put(State state) throws InterruptException {
        try {
            final Reference myObjectRef = (Reference) this.data.operand(0);
            if (state.isNull(myObjectRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Instance myObject = (Instance) state.getObject(myObjectRef);
            myObject.setFieldValue(this.fieldSignatureResolved, this.data.operand(1));
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
