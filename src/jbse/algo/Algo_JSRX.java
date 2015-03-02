package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

final class Algo_JSRX implements Algorithm {
	/** {@code true} for jsr_2, {@code false} for jsr. */
	boolean wide;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
			final int programCounter = state.getPC();
			final byte tmp1 = state.getInstruction(1);
			final byte tmp2 = state.getInstruction(2);
			if (this.wide) {
                final byte tmp3 = state.getInstruction(3);
                final byte tmp4 = state.getInstruction(4);
                state.pushOperand(state.getCalculator().valInt(programCounter + 5));
                state.incPC(Util.byteCat(tmp1, tmp2, tmp3, tmp4));
			} else {
                state.pushOperand(state.getCalculator().valInt(programCounter + 3));
                state.incPC(Util.byteCatShort(tmp1, tmp2));
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}