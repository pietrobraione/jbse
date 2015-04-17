package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

final class Algo_NOTALLOWED implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
        throwVerifyError(state);
	}
}
