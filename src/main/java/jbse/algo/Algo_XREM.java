package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.ARITHMETIC_EXCEPTION;
import static jbse.common.Type.isPrimitiveIntegralOpStack;

import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

//TODO this implementation of remainder (see division) works only for concrete values, add the case for symbolic ones with multiple successors
/**
 * {@link Algorithm} for all the *rem bytecodes
 * ([i/l/f/d]rem).
 * 
 * @author Pietro Braione
 */
final class Algo_XREM extends Algo_BINMATHLOGICALOP {
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final Primitive val1 = (Primitive) this.data.operand(0);
                final Primitive val2 = (Primitive) this.data.operand(1);
                if (isPrimitiveIntegralOpStack(val2.getType())) {
                    if (val2 instanceof Simplex) {
                        final Simplex op0_S = (Simplex) val2;
                        if (op0_S.isZeroOne(true)) {
                            throwNew(state, this.ctx.getCalculator(), ARITHMETIC_EXCEPTION);
                            return;
                        }
                    }
                }
                state.pushOperand(this.ctx.getCalculator().push(val1).rem(val2).pop());
            } catch (ClassCastException | InvalidTypeException | InvalidOperandException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }
}
