package jbse.algo;

import static jbse.algo.Util.createAndThrowObject;
import static jbse.algo.Util.throwObject;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

class SEAthrow implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, OperandStackEmptyException {
        final Reference myExcRef = (Reference) state.pop();
        if (state.isNull(myExcRef)) {
            createAndThrowObject(state, NULL_POINTER_EXCEPTION);
        } else {
            throwObject(state, myExcRef);
        }
    } 
}




