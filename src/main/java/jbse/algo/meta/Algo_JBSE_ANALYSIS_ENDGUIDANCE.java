package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.apps.run.DecisionProcedureGuidance;
import jbse.mem.State;

public final class Algo_JBSE_ANALYSIS_ENDGUIDANCE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }
    
    @Override
    protected void update(State state) {
        if (this.ctx.decisionProcedure instanceof DecisionProcedureGuidance) {
            final DecisionProcedureGuidance dec = (DecisionProcedureGuidance) this.ctx.decisionProcedure;
            dec.endGuidance();
            //System.out.println("***** END GUIDANCE *****"); //TODO log differently!
        }
    }
}
