package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which pushes {@code true} on the operand stack.
 * 
 * @author Pietro Braione
 *
 */
public class Algo_JBSE_ANALYSIS_ISRUNBYJBSE implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, InterruptException {
		state.pushOperand(state.getCalculator().valInt(1)); //boolean is *not* an operand stack type, int is!
        try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
        throw new InterruptException();
	}
}
