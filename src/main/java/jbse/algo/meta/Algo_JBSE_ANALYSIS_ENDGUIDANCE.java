package jbse.algo.meta;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.apps.run.DecisionProcedureGuidance;
import jbse.tree.DecisionAlternative_NONE;

public final class Algo_JBSE_ANALYSIS_ENDGUIDANCE extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            if (this.ctx.decisionProcedure instanceof DecisionProcedureGuidance) {
                final DecisionProcedureGuidance dec = (DecisionProcedureGuidance) this.ctx.decisionProcedure;
                dec.endGuidance();
            }
        };
    }
}
