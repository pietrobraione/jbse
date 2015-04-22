package jbse.algo.meta;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.mem.State;

public class Algo_JBSE_ANALYSIS_SUCCEED implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws InterruptException {
		state.setStuckStop();
		throw InterruptException.getInstance();
	}
}
