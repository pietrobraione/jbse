package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRING_HASH;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Reference;

public class Algo_JAVA_STRING_HASHCODE implements Algorithm {
    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference thisReference = (Reference) state.popOperand();
            final Objekt thisObjekt = state.getObject(thisReference);

            if (thisObjekt.isSymbolic()) {
                //gets the hashCode field in the string and returns it
                final Primitive hashCode = (Primitive) thisObjekt.getFieldValue(JAVA_STRING_HASH);
                //TODO assume hashCode != 0 and ensure that strings that may not be equal have different hash codes
                state.pushOperand(hashCode);
            } else {
                return; //executes the original String.hashCode implementation
            }
        } catch (OperandStackEmptyException e) {
            throwVerifyError(state);
            throw InterruptException.getInstance();
        }
        
        try {
            state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
        }
        throw InterruptException.getInstance();
    }
}
