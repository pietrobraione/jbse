package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

public final class Algo_JAVA_STRING_INTERN extends Algo_INVOKEMETA {
    public Algo_JAVA_STRING_INTERN() {
        super(false);
    }
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final String valueString = valueString(state, (Reference) this.data.operand(0));
                if (valueString == null) {
                    //TODO remove this limitation
                    throw new SymbolicValueNotAllowedException("Cannot intern a symbolic String object.");
                }
                if (state.hasStringLiteral(valueString)) {
                    //nothing to do
                } else {
                    ensureStringLiteral(state, valueString, ctx.decisionProcedure);
                }
                state.pushOperand(state.referenceToStringLiteral(valueString));
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }
        };
    }
}
