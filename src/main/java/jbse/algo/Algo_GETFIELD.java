package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.val.Reference;

/**
 * {@link Algorithm} managing the getfield bytecode. It decides over the value 
 * loaded to the operand stack in the case this is a symbolic reference 
 * ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_GETFIELD extends Algo_GETX {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void check(State state, String currentClass)
    throws FieldNotFoundException, BadClassFileException, InterruptException {
        final String fieldClassName = this.fieldSignatureResolved.getClassName();        
        final ClassFile fieldClassFile = state.getClassHierarchy().getClassFile(fieldClassName);

        //checks that the field is not static
        if (fieldClassFile.isFieldStatic(fieldSignatureResolved)) {
            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }
    }

    @Override
    protected void get(State state) throws InterruptException {
        try {
            final Reference myObjectRef = (Reference) this.data.operand(0);
            if (state.isNull(myObjectRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Instance myObject = (Instance) state.getObject(myObjectRef); 
            this.valToLoad = myObject.getFieldValue(this.fieldSignatureResolved);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }    
}