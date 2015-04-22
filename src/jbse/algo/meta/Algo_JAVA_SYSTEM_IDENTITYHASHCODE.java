package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
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

public class Algo_JAVA_SYSTEM_IDENTITYHASHCODE implements Algorithm {
    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, InterruptException {
        try {
            final Reference thisReference = (Reference) state.popOperand();
            final Objekt thisObjekt = state.getObject(thisReference);

            //gets the hash code stored in the objekt and returns it
            final Primitive hashCode = state.getCalculator().valInt(thisObjekt.getObjektHashCode());
            state.pushOperand(hashCode);
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
