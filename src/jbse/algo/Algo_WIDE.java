package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

final class Algo_WIDE implements Algorithm {
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		state.setWide();
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
