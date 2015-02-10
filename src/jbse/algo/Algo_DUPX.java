package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Type;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class Algo_DUPX implements Algorithm {
	/** {@code true} for dup, {@code false} for dup2. */
	boolean cat1;
	
	public Algo_DUPX() { }
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		boolean error = false;

		final Value tmp1 = state.pop();
		if (this.cat1) {
			//dup
			if (Type.isCat_1(tmp1.getType())) {
				//dup semantics
				state.push(tmp1);
				state.push(tmp1);
			} else {
				//dup: incorrect operand
				error = true;
			}
		} else {
			//dup2
			if (Type.isCat_1(tmp1.getType())) {
				//dup2: we need a second operand 
				final Value tmp2 = state.pop();
				if (Type.isCat_1(tmp2.getType())) {
					//dup2 form 1 semantics
					state.push(tmp2);
					state.push(tmp1);
					state.push(tmp2);
					state.push(tmp1);
				} else {
					//dup2: incorrect operand 2
					error = true;
				}
			} else {
				//dup2 form 2 semantics 
				state.push(tmp1);
				state.push(tmp1);
			}
		}

        //common updates
        if (error) {
            throwVerifyError(state);
        }

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
		    throwVerifyError(state);
		}
	}
}
