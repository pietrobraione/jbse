package jbse.rewr;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.exc.NoResultException;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * A {@link Rewriter} which normalizes application of trigonometric
 * functions {@code f(X + k)}, with k numeric and f = +/- sin, cos, or tan, by 
 * reducing it to a suitable equivalent {@code g(X + k')}, where 
 * {@code 0 <= k'} and {@code k' < }{@link Math#PI PI}.
 * For instance, reduces {@code sin(X + 5*}{@link Math#PI PI}{@code )} to 
 * {@code -sin(X)}.
 * 
 * @author Pietro Braione
 *
 */
public class RewriterTrigNormalize extends Rewriter {
	public RewriterTrigNormalize() { }

	@Override
	protected void rewriteFunctionApplication(FunctionApplication x)
	throws NoResultException {
		final String operator = x.getOperator();
		final double period;
		if (operator.equals(FunctionApplication.TAN)) {
			period = Math.PI;
		} else if (operator.equals(FunctionApplication.SIN) ||
				operator.equals(FunctionApplication.COS)) {
			period = 2 * Math.PI;
		} else {
			super.rewriteFunctionApplication(x);
			return;
		}
		if (x.getType() != Type.DOUBLE) {
			//trigonometric function yields nondouble value; in doubt we give up
			super.rewriteFunctionApplication(x);
			return;
		}
		if (x.getArgs().length != 1) {
			//trigonometric function with strange number of args;
			//since it is not our business complaining we just give up
			super.rewriteFunctionApplication(x);
			return;
		}
		Polynomial arg = Polynomial.of(this.calc, x.getArgs()[0]);
		double addend = ((Number)arg.getConstantTerm().getActualValue()).doubleValue();
		final boolean normalized, negate;
		try {
			if (addend < 0 || addend >= period) {
				normalized = true;
				final Polynomial periodP = Polynomial.of(this.calc, this.calc.valDouble(period));
				final Polynomial nperiodP = Polynomial.of(this.calc, this.calc.valDouble(-period));
				do {
					if (addend < 0) {
						arg = arg.add(periodP);
						addend += period;
					} else {
						arg = arg.add(nperiodP);
						addend -= period;
					}
				} while (addend < 0 || addend >= period);
			} else {
				normalized = false;
			}
			if ((operator.equals(FunctionApplication.SIN) ||
					operator.equals(FunctionApplication.COS)) && addend >= Math.PI) {
				negate = true;
				final Polynomial nPiP = Polynomial.of(this.calc, this.calc.valDouble(-Math.PI));
				try {
					arg = arg.add(nPiP);
				} catch (InvalidTypeException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
			} else {
				negate = false;
			}
			if (normalized || negate) {
				Primitive result = this.calc.applyFunction(x.getType(), operator, arg.toPrimitive());
				if (negate) {
					result = result.neg();
				}
				setResult(result);
			} else {
				super.rewriteFunctionApplication(x);
			}
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
