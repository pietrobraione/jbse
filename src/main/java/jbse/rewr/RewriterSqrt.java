package jbse.rewr;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites {@code sqrt(X * X * Y)} to {@code abs(X) * sqrt(Y)}
 * and {@code sqrt(X * X +- 2 * X * Y + Y * Y)} to {@code abs(X +- Y)}.
 * Requires {@link RewriterOperationOnSimplex}.
 * 
 * @author Pietro Braione
 */
public class RewriterSqrt extends RewriterCalculatorRewriting {
	public RewriterSqrt() { }
	
	@Override
	protected void rewritePrimitiveSymbolicApply(PrimitiveSymbolicApply x)
	throws NoResultException {
		if (x.getOperator().equals(PrimitiveSymbolicApply.SQRT)) {
			if (x.getType() != Type.DOUBLE) {
				//sqrt function yielding nondouble value; in doubt we give up
				setResult(x);
				return;
			}
			if (x.getArgs().length != 1) {
				//sqrt function with strange number of args;
				//since complaining is not our business we just give up
				setResult(x);
				return;
			}
			final Primitive arg = (Primitive) x.getArgs()[0];
			if (arg.getType() != Type.DOUBLE) {
				//sqrt function yielding double value but with nondouble arg; 
				//since complaining is not our business we just give up
				setResult(x);
				return;
			}
			final Polynomial[] argSqrt;
			try {
				argSqrt = Polynomial.of(this.calc, arg).sqrt(this.calc);
				if (argSqrt[0].isZeroOne(false)) {
					super.rewritePrimitiveSymbolicApply(x);
				} else {
					setResult(this.calc.applyFunctionPrimitive(x.getType(), x.historyPoint(), PrimitiveSymbolicApply.ABS_DOUBLE, argSqrt[0].toPrimitive(this.calc))
							.mul(this.calc.applyFunctionPrimitiveAndPop(x.getType(), x.historyPoint(), PrimitiveSymbolicApply.SQRT, argSqrt[1].toPrimitive(this.calc))).pop());
				}
			} catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		} else {
			setResult(x);
		}
	}
}
