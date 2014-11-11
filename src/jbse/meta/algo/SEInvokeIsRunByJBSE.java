package jbse.meta.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;
import jbse.algo.Algorithm;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;


/**
 * An {@link Algorithm} implementing the effect of a method call
 * which pushes {@code true} on the operand stack.
 * 
 * @author Pietro Braione
 *
 */
public class SEInvokeIsRunByJBSE implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, UnexpectedInternalException {
		state.push(state.getCalculator().valInt(1)); //boolean is *not* an operand stack type, int is!
        try {
			state.incPC(INVOKESTATIC_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
		}
	}
}
