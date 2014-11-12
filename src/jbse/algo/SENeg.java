package jbse.algo;

import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Primitive;
import jbse.mem.State;
import jbse.mem.Value;

final class SENeg implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		final Value op = state.pop();
		try {
			state.push(((Primitive)op).neg());
		} catch (InvalidTypeException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
	}
}
