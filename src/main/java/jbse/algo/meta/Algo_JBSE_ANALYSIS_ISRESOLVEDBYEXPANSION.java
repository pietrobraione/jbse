package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.mem.Util.isResolved;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Simplex;

public final class Algo_JBSE_ANALYSIS_ISRESOLVEDBYEXPANSION extends Algo_INVOKEMETA_Nonbranching {
    private Simplex retVal; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, InvalidInputException {
        Reference ref = null; //to keep the compiler happy
        try {
            ref = (Reference) this.data.operand(0);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        
        if (!isResolved(state, ref)) {
        	//this should never happen
        	failExecution("Found an unresolved reference on the operand stack.");
        }

        //analyzes the field and calculates the return value
        if (ref.isSymbolic()) {
        	int retVal = 0;
        	for (Clause c : state.getPathCondition()) {
        		if (c instanceof ClauseAssumeExpands) {
        			final ClauseAssumeExpands cExp = (ClauseAssumeExpands) c;
        			if (cExp.getReference() == ref) {
        				retVal = 1;
        				break;
        			}
        		}
        	}
            this.retVal = this.ctx.getCalculator().valInt(retVal);
        } else {
            this.retVal = this.ctx.getCalculator().valInt(0);
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.retVal);
        };
    }
}
