package jbse.algo;

import jbse.Type;
import jbse.Util;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;
import jbse.mem.Value;

final class SEPop implements Algorithm {
	/** {@code true} for pop, {@code false} for pop2 */ 
	boolean def;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		Value tmp = state.pop();
		if (def && !Type.isCat_1(tmp.getType())) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		if (!def && Type.isCat_1(tmp.getType())) {
			tmp = state.pop();
			if (!Type.isCat_1(tmp.getType())) {
				state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
				return;
			}
		}
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
