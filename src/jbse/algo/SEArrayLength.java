package jbse.algo;

import static jbse.algo.Util.NULL_POINTER_EXCEPTION;
import static jbse.bc.Offsets.ARRAYLENGTH_OFFSET;
import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Array;
import jbse.mem.Reference;
import jbse.mem.State;

class SEArrayLength implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, OperandStackEmptyException, UnexpectedInternalException {
        final Reference tmpRef = (Reference) state.pop();
        if (state.isNull(tmpRef)) {
        	state.createThrowableAndThrowIt(NULL_POINTER_EXCEPTION);
			return;
        }
        
        final Array tmpArray = (Array) state.getObject(tmpRef);
        state.push(tmpArray.getLength());

        try {
			state.incPC(ARRAYLENGTH_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
    } 
}
