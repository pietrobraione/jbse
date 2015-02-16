package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

public class Algo_JBSE_ANALYSIS_IGNORE implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ContradictionException, ThreadStackEmptyException {
		if (state.mayViolateAssumption()) {
			throw new ContradictionException();
		} else {
	        try {
				state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
			} catch (InvalidProgramCounterException e) {
	            throwVerifyError(state);
			}
		}
	}
}
