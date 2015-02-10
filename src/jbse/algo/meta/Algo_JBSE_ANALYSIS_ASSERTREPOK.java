package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public final class Algo_JBSE_ANALYSIS_ASSERTREPOK implements Algorithm {

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException {
		//pops the parameters and stores them in ctx
		//TODO store them elsewhere and eliminate the dependence of Run from ExecutionContext
		final Reference methodNameRef = (Reference) state.pop();
		ctx.repOkMethodName = valueString(state, methodNameRef);
		ctx.repOkTargetObjectReference = (Reference) state.pop();

        try {
			state.incPC(INVOKESTATIC_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}
