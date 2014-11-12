package jbse.algo.nativ;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;

public class SEGetStackTraceDepth implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//TODO replace this dummy implementation
		state.pop(); //pops "this"
		state.push(state.getCalculator().valInt(0));
		
        try {
			state.incPC(INVOKEVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
		}
	}
}
