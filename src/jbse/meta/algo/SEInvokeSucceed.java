package jbse.meta.algo;

import jbse.algo.Algorithm;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

public class SEInvokeSucceed implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) {
		state.setStuckStop();
	}
}
