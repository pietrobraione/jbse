package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Type;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class Algo_POPX implements Algorithm {
	/** {@code true} for pop, {@code false} for pop2 */ 
	boolean cat1;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
	    try {
	        Value tmp = state.pop();
	        if (this.cat1 && !Type.isCat_1(tmp.getType())) {
	            throwVerifyError(state);
	            return;
	        }

	        if (!this.cat1 && Type.isCat_1(tmp.getType())) {
	            tmp = state.pop();
	            if (!Type.isCat_1(tmp.getType())) {
	                throwVerifyError(state);
	                return;
	            }
	        }
	    } catch (OperandStackEmptyException e) {
	        throwVerifyError(state);
	        return;
	    }
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
