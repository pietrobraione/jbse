package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

final class Algo_GOTOX implements Algorithm {
	/** {@code true} for goto_w, {@code false} for goto. */
	boolean wide;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			final byte tmp0 = state.getInstruction(1);
			final byte tmp1 = state.getInstruction(2);
			if (this.wide) {
                final byte tmp2 = state.getInstruction(3);
                final byte tmp3 = state.getInstruction(4);
                state.incPC(Util.byteCat(tmp0, tmp1, tmp2, tmp3));
			} else {
                state.incPC(Util.byteCatShort(tmp0, tmp1));
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
