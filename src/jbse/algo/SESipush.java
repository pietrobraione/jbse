package jbse.algo;

import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

/**
 * Command managing the "push short" (sipush) bytecode. 
 * 
 * @author unknown
 * @author Pietro Braione
 */
final class SESipush implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			final byte tmp0 = state.getInstruction(1);
			final byte tmp1 = state.getInstruction(2);
			state.push(state.getCalculator().valInt((int) Util.byteCatShort(tmp0, tmp1)));
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}

		try {
			state.incPC(3);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
