package jbse.meta.algo;

import jbse.algo.Algorithm;
import jbse.exc.jvm.FailureException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

public class SEInvokeFail implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) throws FailureException {
		throw new FailureException();
	}
}
