package jbse.algo;

import static jbse.algo.Util.NULL_POINTER_EXCEPTION;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.throwIt;

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
            createAndThrow(state, NULL_POINTER_EXCEPTION);
        } else {
            throwIt(state, myExcRef);
        }
    } 
}




