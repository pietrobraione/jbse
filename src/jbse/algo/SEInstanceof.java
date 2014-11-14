package jbse.algo;

import static jbse.algo.Util.checkCastInstanceof;
import static jbse.algo.Util.createAndThrow;
import static jbse.algo.Util.throwVerifyError;

import jbse.algo.exc.JavaReifyException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

final class SEInstanceof implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		final boolean isSubclass;
		try {
			isSubclass = checkCastInstanceof(state);
		} catch (JavaReifyException e) {
            createAndThrow(state, e.subject());
			return;
		}
		
		//pops the checked reference and calculates the result
		final Reference tmpValue = (Reference) state.pop();
		if (!state.isNull(tmpValue) && isSubclass) { 
			state.push(state.getCalculator().valInt(1));
		} else { 
			state.push(state.getCalculator().valInt(0));
		}

		//increments the program counter
		try {
			state.incPC(3);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		} 
	}
}
