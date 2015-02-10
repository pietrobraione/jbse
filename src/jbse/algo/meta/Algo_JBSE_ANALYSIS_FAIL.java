package jbse.algo.meta;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;

public class Algo_JBSE_ANALYSIS_FAIL implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) throws FailureException {
		throw new FailureException();
	}
}
