package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;

import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} for all the *shl bytecodes
 * ([i/l]shl).
 * 
 * @author Pietro Braione
 */
final class Algo_XSHL extends Algo_BINMATHLOGICALOP {
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final Primitive val1 = (Primitive) this.data.operand(0);
                final Primitive val2 = (Primitive) this.data.operand(1);
                state.pushOperand(this.ctx.getCalculator().push(val1).shl(val2).pop());
            } catch (ClassCastException | InvalidTypeException | InvalidOperandException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }
}
