package jbse.meta.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;

import jbse.algo.Algorithm;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;


public class SEInvokeIgnore implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ContradictionException, ThreadStackEmptyException {
		if (state.mayViolateAssumption()) {
			throw new ContradictionException();
		} else {
	        try {
				state.incPC(INVOKESTATIC_OFFSET);
			} catch (InvalidProgramCounterException e) {
				state.createThrowableAndThrowIt(VERIFY_ERROR);
			}
		}
	}
}
