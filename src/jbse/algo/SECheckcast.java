package jbse.algo;

import static jbse.algo.Util.CLASS_CAST_EXCEPTION;
import static jbse.algo.Util.checkCastInstanceof;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.throwVerifyError;

import jbse.algo.exc.JavaReifyException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

class SECheckcast implements Algorithm {

	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
    	final boolean isSubclass;
    	try {
    		isSubclass = checkCastInstanceof(state);
    	} catch (JavaReifyException e) {
            createAndThrow(state, e.subject());
			return;
    	}
    		
    	//if the check fails throws a ClassCastException
    	if (!isSubclass) {
            createAndThrow(state, CLASS_CAST_EXCEPTION);
    		return;
    	}

    	try {
    		//increments the program counter
    		state.incPC(3);
    	} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
    	}
    }
}