package jbse.algo;

import static jbse.algo.Util.NULL_POINTER_EXCEPTION;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Reference;
import jbse.mem.State;

class SEAthrow implements Algorithm {
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, OperandStackEmptyException, UnexpectedInternalException {
        Reference myExcRef = (Reference) state.pop();
        if (state.isNull(myExcRef)) {
        	myExcRef = state.createInstance(NULL_POINTER_EXCEPTION);
        }
        state.throwIt(myExcRef);
    } 
}




