package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

final class SEGoto implements Algorithm {
	/** {@code true} for goto, {@code false} for goto_w. */
	boolean def;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			final byte tmp0 = state.getInstruction(1);
			final byte tmp1 = state.getInstruction(2);
			if (def) {
				state.incPC(Util.byteCatShort(tmp0, tmp1));
			} else {
				final byte tmp2 = state.getInstruction(3);
				final byte tmp3 = state.getInstruction(4);
				state.incPC(Util.byteCat(tmp0, tmp1, tmp2, tmp3));
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
