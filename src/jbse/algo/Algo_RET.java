package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Util;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Simplex;

final class Algo_RET implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		boolean wide = state.nextWide();
		
		try {
			int index;
			byte tmp1 = state.getInstruction(1);
			if (wide) {
				byte tmp2 = state.getInstruction(2);
				index = Util.byteCat(tmp1, tmp2);
			} else {
				index = tmp1;
			}
			final Simplex ret;
			try {
				ret = (Simplex) state.getLocalVariableValue(index);
			} catch (InvalidSlotException e) {
	            throwVerifyError(state);
				return;
			}
			state.setPC(((Integer) ret.getActualValue()).intValue());
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
	} 
}
