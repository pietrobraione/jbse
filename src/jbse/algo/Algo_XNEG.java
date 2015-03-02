package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.exc.InvalidTypeException;

final class Algo_XNEG implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		try {
	        final Primitive value = (Primitive) state.popOperand();
			state.pushOperand(value.neg());
		} catch (OperandStackEmptyException | ClassCastException | 
		         InvalidTypeException e) {
            throwVerifyError(state);
			return;
		}
		
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
	}
}
