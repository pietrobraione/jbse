package jbse.algo;

import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

/**
 * Command managing the "push byte" (bipush) bytecode. 
 * 
 * @author unknown
 * @author Pietro Braione
 */
class SEBipush implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, UnexpectedInternalException {
        try {
        	state.push(state.getCalculator().valInt((int) state.getInstruction(1)));
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		try {
			state.incPC(2);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
    } 
}
