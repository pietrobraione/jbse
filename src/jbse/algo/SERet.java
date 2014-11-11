package jbse.algo;

import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Simplex;
import jbse.mem.State;

class SERet implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws UnexpectedInternalException, ThreadStackEmptyException {
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
				state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
				return;
			}
			state.setPC(((Integer) ret.getActualValue()).intValue());
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
	} 
}
