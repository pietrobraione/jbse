package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * Command managing the "push byte" (bipush) bytecode. 
 * 
 * @author unknown
 * @author Pietro Braione
 */
class Algo_BIPUSH implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
        try {
        	state.push(state.getCalculator().valInt((int) state.getInstruction(1)));
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
		
		try {
			state.incPC(2);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
    } 
}
