package jbse.algo;

import static jbse.algo.Util.NULL_POINTER_EXCEPTION;

import jbse.Util;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Reference;
import jbse.mem.State;
import jbse.mem.Value;

class SEMonitor implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws OperandStackEmptyException, ThreadStackEmptyException, 
	DecisionException {
		//pops its operand and checks it
		final Value v = state.pop();
		if (!(v instanceof Reference)) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		final Reference r = (Reference) v;
		if (state.isNull(r)) {
			state.createThrowableAndThrowIt(NULL_POINTER_EXCEPTION);
			return;
		}
		
		//TODO jbse is single-threading, extend it to multithreading?

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
