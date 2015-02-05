package jbse.algo;

import static jbse.algo.Util.createAndThrowObject;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Value;

class SEMonitor implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws OperandStackEmptyException, ThreadStackEmptyException, 
	DecisionException {
		//pops its operand and checks it
		final Value v = state.pop();
		if (!(v instanceof Reference)) {
            throwVerifyError(state);
			return;
		}
		final Reference r = (Reference) v;
		if (state.isNull(r)) {
            createAndThrowObject(state, NULL_POINTER_EXCEPTION);
			return;
		}
		
		//TODO jbse is single-threading, extend it to multithreading?

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
