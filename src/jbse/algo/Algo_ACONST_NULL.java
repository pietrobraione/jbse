package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XCONST_OFFSET;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;


final class Algo_ACONST_NULL implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
        state.push(Null.getInstance());
        try {
			state.incPC(XCONST_OFFSET);
		} catch (InvalidProgramCounterException e) {
		    throwVerifyError(state);
	    	return;
		}
    } 
}
