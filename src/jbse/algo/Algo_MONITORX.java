package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

class Algo_MONITORX implements Algorithm {
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, DecisionException {
		//pops its operand and checks it
		final Reference v;
		try {
		    v = (Reference) state.popOperand();
		} catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            return;
		}
		if (state.isNull(v)) {
            throwNew(state, NULL_POINTER_EXCEPTION);
			return;
		}
		
		//nothing to do, JBSE is single-threading
		//TODO when a multithreading JBSE?

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
