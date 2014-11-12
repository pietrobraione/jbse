package jbse.algo;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;
import jbse.mem.Value;

final class SEReturn implements Algorithm {
	/** {@code true} iff returning from a method with void return type. */
	boolean def;
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		if (def) {
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
