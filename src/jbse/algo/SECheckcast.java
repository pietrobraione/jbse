package jbse.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.algo.Util.CLASS_CAST_EXCEPTION;
import static jbse.algo.Util.checkCastInstanceof;

import jbse.exc.algo.JavaReifyException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

class SECheckcast implements Algorithm {

	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
    	final boolean isSubclass;
    	try {
    		isSubclass = checkCastInstanceof(state);
    	} catch (JavaReifyException e) {
			state.createThrowableAndThrowIt(e.subject());
			return;
    	}
    		
    	//if the check fails throws a ClassCastException
    	if (!isSubclass) {
    		state.createThrowableAndThrowIt(CLASS_CAST_EXCEPTION);
    		return;
    	}

    	try {
    		//increments the program counter
    		state.incPC(3);
    	} catch (InvalidProgramCounterException e) {
    		state.createThrowableAndThrowIt(VERIFY_ERROR);
    	}
    }
}