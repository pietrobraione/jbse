package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class Algo_SWAP implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
	    try {
	        final Value tmp2 = state.popOperand();
	        final Value tmp1 = state.popOperand();
	        state.pushOperand(tmp2);
	        state.pushOperand(tmp1);
	    } catch (OperandStackEmptyException e) {
	        throwVerifyError(state);
	        return;
	    }

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
