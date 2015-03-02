package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Command managing the "push short" (sipush) bytecode. 
 * 
 * @author unknown
 * @author Pietro Braione
 */
final class Algo_SIPUSH implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			final byte tmp0 = state.getInstruction(1);
			final byte tmp1 = state.getInstruction(2);
			state.pushOperand(state.getCalculator().valInt((int) Util.byteCatShort(tmp0, tmp1)));
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}

		try {
			state.incPC(3);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
