package jbse.algo;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class Algo_XRETURN implements Algorithm {
	/** {@code true} iff returning from a method with void return type. */
	boolean returnVoid;
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		if (returnVoid) {
			state.popCurrentFrame();
			if (state.getStackSize() == 0) {
				state.setStuckReturn();
				return;
			}
		} else {
			final Value retValue = state.pop();
			state.popCurrentFrame();
			if (state.getStackSize() == 0) {
				state.setStuckReturn(retValue);
				return;
			}
			state.push(retValue);
		}
		
		try {
			state.useReturnPC();
		} catch (InvalidProgramCounterException e) {
			throw new UnexpectedInternalException(e);
		}
	} 
}
