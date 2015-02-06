package jbse.algo.nativ;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;

public final class SEGetStackTraceDepth implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//TODO replace this dummy implementation
		state.pop(); //pops "this"
		state.push(state.getCalculator().valInt(0));
		
        try {
			state.incPC(INVOKEVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
