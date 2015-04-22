package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.algo.exc.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.FieldNotFoundException;
import jbse.mem.Instance;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * Command managing the getfield bytecode. It decides over the value 
 * loaded to the operand stack in the case this is a symbolic reference 
 * ("lazy initialization").
 * 
 * @author Pietro Braione
 */
final class Algo_GETFIELD extends Algo_GETX {
    @Override
    protected boolean fieldOk(Signature fieldSignatureResolved, ClassFile fieldClassFile)
    throws FieldNotFoundException {
        return !(fieldClassFile.isFieldStatic(fieldSignatureResolved));
    }

    @Override
    protected Value fieldValue(Signature fieldSignatureResolved) 
    throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference myObjectRef = (Reference) this.state.popOperand();
            if (this.state.isNull(myObjectRef)) {
                throwNew(this.state, NULL_POINTER_EXCEPTION);
                throw InterruptException.getInstance();
            }
            final Instance myObject = (Instance) this.state.getObject(myObjectRef); 
            return myObject.getFieldValue(fieldSignatureResolved);
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            throw InterruptException.getInstance();
        }
    }    
}