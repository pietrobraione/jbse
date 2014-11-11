package jbse.algo;

import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

class SEJsr implements Algorithm {
	/** {@code true} for jsr, {@code false} for jsr_w. */
	boolean def;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, UnexpectedInternalException {
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}