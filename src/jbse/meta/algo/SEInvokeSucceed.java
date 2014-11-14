package jbse.meta.algo;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.mem.State;

public class SEInvokeSucceed implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) {
		state.setStuckStop();
	}
}
