package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwObject;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

class Algo_ATHROW implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) throws ThreadStackEmptyException {
	    try {
	        final Reference myExcRef = (Reference) state.popOperand();
	        if (state.isNull(myExcRef)) {
	            throwNew(state, NULL_POINTER_EXCEPTION);
	        } else {
	            throwObject(state, myExcRef);
	        }
	    } catch (OperandStackEmptyException | ClassCastException e) {
	        throwVerifyError(state);
        }	    
    } 
}




