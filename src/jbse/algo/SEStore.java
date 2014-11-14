package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XLOADSTORE_WIDE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_OFFSET;

import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class SEStore implements Algorithm {
	boolean def;
	int index;

	public SEStore() { }

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		final boolean wide = state.nextWide();
		
		final Value valTemp = state.pop();
		try {
			if (!def) {
				if (wide) {
					final byte tmp1 = state.getInstruction(1);
					final byte tmp2 = state.getInstruction(2);
					this.index = Util.byteCat(tmp1, tmp2);
				} else {
					this.index = state.getInstruction(1);
				}
			}
			try {
				state.setLocalVariable(this.index, valTemp);
			} catch (InvalidSlotException e) {
				if (def) {
					throw new UnexpectedInternalException(e);
				} else {
		            throwVerifyError(state);
					return;
				}
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}

		try {
			if (def) {
				state.incPC();
			} else if (wide) {
				state.incPC(XLOADSTORE_WIDE_OFFSET);
			} else {
				state.incPC(XLOADSTORE_IMMEDIATE_OFFSET);
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	} 
}
