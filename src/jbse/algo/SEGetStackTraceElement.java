package jbse.algo;

import static jbse.algo.Util.createAndThrowObject;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;

import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;

public class SEGetStackTraceElement implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//TODO replace this dummy implementation
		state.pop();
		state.pop();
        createAndThrowObject(state, INDEX_OUT_OF_BOUNDS_EXCEPTION);
	}
}
