package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

final class SEJsr implements Algorithm {
	/** {@code true} for jsr, {@code false} for jsr_w. */
	boolean def;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			final int programCounter = state.getPC();
			final byte tmp1 = state.getInstruction(1);
			final byte tmp2 = state.getInstruction(2);
			if (def) {
				state.push(state.getCalculator().valInt(programCounter + 3));
				state.incPC(Util.byteCatShort(tmp1, tmp2));
			} else {
				final byte tmp3 = state.getInstruction(3);
				final byte tmp4 = state.getInstruction(4);
				state.push(state.getCalculator().valInt(programCounter + 5));
				state.incPC(Util.byteCat(tmp1, tmp2, tmp3, tmp4));
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}