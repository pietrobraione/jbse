package jbse.meta.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which disables violation of assumptions.
 * 
 * @author Pietro Braione
 *
 */
public final class SEInvokeDisableAssumptionViolation implements Algorithm {

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		state.disableAssumptionViolation();
        try {
			state.incPC(INVOKESTATIC_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
