package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

final class SENop implements Algorithm {
	public SENop() { }

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	} 
}