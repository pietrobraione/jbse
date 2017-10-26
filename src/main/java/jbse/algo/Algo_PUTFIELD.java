package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.FieldNotFoundException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.val.Reference;
import jbse.val.Value;

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
    protected void checkMore(State state, String fieldClassName, ClassFile fieldClassFile)
    throws FieldNotFoundException, InterruptException {
        //checks that the field is not static
        if (fieldClassFile.isFieldStatic(this.fieldSignatureResolved)) {
            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected Value valueToPut() {
        return this.data.operand(1);
    }

    @Override
    protected Objekt destination(State state) throws InterruptException {
        try {
            final Reference myObjectRef = (Reference) this.data.operand(0);
            if (state.isNull(myObjectRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            return state.getObject(myObjectRef);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
        return null; //to keep the compiler happy
    }
}
