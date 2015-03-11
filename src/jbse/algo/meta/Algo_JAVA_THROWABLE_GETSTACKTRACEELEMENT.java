package jbse.algo.meta;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, InterruptException {
		//TODO replace this dummy implementation
	    try {
	        state.popOperand();
	        state.popOperand();
	    } catch (OperandStackEmptyException e) {
	        throwVerifyError(state);
            throw new InterruptException();
	    }
        throwNew(state, INDEX_OUT_OF_BOUNDS_EXCEPTION);
        throw new InterruptException();
	}
}
