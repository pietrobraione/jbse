package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;

public final class Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, InterruptException {
		//TODO replace this dummy implementation
		try {
            state.popOperand();  //pops "this"
        } catch (OperandStackEmptyException e) {
            throwVerifyError(state);
            throw InterruptException.getInstance();
        }
		state.pushOperand(state.getCalculator().valInt(0));
		
        try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
        throw InterruptException.getInstance();
	}
}
