package jbse.algo;

import static jbse.algo.Util.checkCastInstanceof;

import jbse.Util;
import jbse.exc.algo.JavaReifyException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Reference;
import jbse.mem.State;

class SEInstanceof implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException, UnexpectedInternalException {
		final boolean isSubclass;
		try {
			isSubclass = checkCastInstanceof(state);
		} catch (JavaReifyException e) {
			state.createThrowableAndThrowIt(e.subject());
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		} 
	}
}






