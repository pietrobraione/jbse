package jbse.algo;

import static jbse.bc.Offsets.XCONST_OFFSET;

import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Null;
import jbse.mem.State;


final class SEAconst_null implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
        state.push(Null.getInstance());
        try {
			state.incPC(XCONST_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
	    	return;
		}
    } 
}
