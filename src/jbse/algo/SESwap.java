package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class SESwap implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		final Value tmp2 = state.pop();
		final Value tmp1 = state.pop();
		state.push(tmp2);
		state.push(tmp1);

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
