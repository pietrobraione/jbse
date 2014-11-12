package jbse.algo;

import jbse.Util;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

final class SEUnexpected implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
	}
}
