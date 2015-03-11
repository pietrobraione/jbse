package jbse.algo.meta;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.InterruptException;
import jbse.apps.run.DecisionProcedureGuidance;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

public class Algo_JBSE_ANALYSIS_ENDGUIDANCE implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx)
	throws CannotManageStateException, ThreadStackEmptyException, 
	ContradictionException, InterruptException {
		if (ctx.decisionProcedure instanceof DecisionProcedureGuidance) {
			final DecisionProcedureGuidance dec = (DecisionProcedureGuidance) ctx.decisionProcedure;
			dec.endGuidance();
			//System.out.println("***** END GUIDANCE *****"); //TODO log differently!
		}
		
		try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
        throw new InterruptException();
	}
}
