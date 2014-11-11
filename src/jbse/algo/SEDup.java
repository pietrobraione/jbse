package jbse.algo;

import jbse.Type;
import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;
import jbse.mem.Value;

class SEDup implements Algorithm {
	/** {@code true} for dup, {@code false} for dup2. */
	boolean cat_1;
	
	public SEDup() { }
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException, UnexpectedInternalException {
		boolean error = false;

		final Value tmp1 = state.pop();
		if (this.cat_1) {
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

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			error = true;
		}

		//common updates
		if (error) {
	    	state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
	}
}
