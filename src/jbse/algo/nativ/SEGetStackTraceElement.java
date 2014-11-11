package jbse.algo.nativ;

import static jbse.algo.Util.INDEX_OUT_OF_BOUNDS_EXCEPTION;
import jbse.algo.Algorithm;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

public class SEGetStackTraceElement implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException, UnexpectedInternalException {
		//TODO replace this dummy implementation
		state.pop();
		state.pop();
		state.createThrowableAndThrowIt(INDEX_OUT_OF_BOUNDS_EXCEPTION);
	}
}
