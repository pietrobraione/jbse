package jbse.algo;

import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

final class SENop implements Algorithm {
	public SENop() { }

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	} 
}