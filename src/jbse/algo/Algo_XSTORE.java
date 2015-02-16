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

final class Algo_XSTORE implements Algorithm {
	boolean hasIndex;
	int index;

	public Algo_XSTORE() { }

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		final boolean wide = state.nextWide();
		
		try {
	        final Value valTemp = state.pop();
			if (!this.hasIndex) {
				if (wide) {
					final byte tmp1 = state.getInstruction(1);
					final byte tmp2 = state.getInstruction(2);
					this.index = Util.byteCat(tmp1, tmp2);
				} else {
					this.index = state.getInstruction(1);
				}
			}
            state.setLocalVariable(this.index, valTemp);
		} catch (OperandStackEmptyException | InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
        } catch (InvalidSlotException e) {
            if (this.hasIndex) {
                throw new UnexpectedInternalException(e);
            } else {
                throwVerifyError(state);
                return;
            }
		}

		try {
			if (this.hasIndex) {
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
