package jbse.algo;

import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

final class SEWide implements Algorithm {
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		state.setWide();
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
