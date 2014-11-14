package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Type;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class SEPop implements Algorithm {
	/** {@code true} for pop, {@code false} for pop2 */ 
	boolean def;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		Value tmp = state.pop();
		if (def && !Type.isCat_1(tmp.getType())) {
            throwVerifyError(state);
			return;
		}
		
		if (!def && Type.isCat_1(tmp.getType())) {
			tmp = state.pop();
			if (!Type.isCat_1(tmp.getType())) {
	            throwVerifyError(state);
				return;
			}
		}
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
