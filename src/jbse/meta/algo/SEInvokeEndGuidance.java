package jbse.meta.algo;

import static jbse.Util.VERIFY_ERROR;
import static jbse.bc.Offsets.INVOKESTATIC_OFFSET;
import jbse.algo.Algorithm;
import jbse.apps.run.DecisionProcedureGuidance;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;


public class SEInvokeEndGuidance implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx)
	throws CannotManageStateException, ThreadStackEmptyException, 
	ContradictionException, UnexpectedInternalException {
		if (ctx.decisionProcedure instanceof DecisionProcedureGuidance) {
			final DecisionProcedureGuidance dec = (DecisionProcedureGuidance) ctx.decisionProcedure;
			dec.endGuidance();
			//System.out.println("***** END GUIDANCE *****"); //TODO log differently!
		}
		try {
			state.incPC(INVOKESTATIC_OFFSET);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(VERIFY_ERROR);
		}
	}
}
