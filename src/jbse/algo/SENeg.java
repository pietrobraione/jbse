package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

final class SENeg implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		final Value op = state.pop();
		try {
			state.push(((Primitive)op).neg());
		} catch (InvalidTypeException e) {
            throwVerifyError(state);
			return;
		}
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
	}
}
