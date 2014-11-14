package jbse.rewr;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.exc.NoResultException;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Rewrites {@code sqrt(X * X * Y)} to {@code abs(X) * sqrt(Y)}
 * and {@code sqrt(X * X +- 2 * X * Y + Y * Y)} to {@code abs(X +- Y)}.
 * Requires {@link RewriterOperationOnSimplex}.
 * 
 * @author Pietro Braione
 */
public class RewriterSqrt extends Rewriter {
	public RewriterSqrt() { }
	
	@Override
	protected void rewriteFunctionApplication(FunctionApplication x)
	throws NoResultException {
		if (x.getOperator().equals(FunctionApplication.SQRT)) {
			if (x.getType() != Type.DOUBLE) {
				//sqrt function yielding nondouble value; in doubt we give up
				super.rewriteFunctionApplication(x);
				return;
			}
			if (x.getArgs().length != 1) {
				//sqrt function with strange number of args;
				//since complaining is not our business we just give up
				super.rewriteFunctionApplication(x);
				return;
			}
			final Primitive arg = x.getArgs()[0];
			final Polynomial[] argSqrt;
			try {
				argSqrt = Polynomial.of(this.calc, arg).sqrt();
				if (argSqrt[0].isZeroOne(false)) {
					super.rewriteFunctionApplication(x);
				} else {
					setResult(this.calc.applyFunction(x.getType(), FunctionApplication.ABS, argSqrt[0].toPrimitive())
							.mul(this.calc.applyFunction(x.getType(), FunctionApplication.SQRT, argSqrt[1].toPrimitive())));
				}
			} catch (InvalidOperandException | InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		} else {
			super.rewriteFunctionApplication(x);
		}
	}
}
