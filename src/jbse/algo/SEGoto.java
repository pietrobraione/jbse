package jbse.algo;

import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

class SEGoto implements Algorithm {
	/** {@code true} for goto, {@code false} for goto_w. */
	boolean def;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, UnexpectedInternalException {
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
