package jbse.rewr;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.rewr.NoResultException;
import jbse.mem.Expression;
import jbse.mem.FunctionApplication;
import jbse.mem.Operator;
import jbse.mem.Primitive;

/**
 * A {@link Rewriter} which rewrites {@code sin(X) / cos(X)} to {@code tan(X)}.
 * Does not work with, e.g., {@code (sin(X) * A) / cos(X)}.
 * 
 * @author Pietro Braione
 *
 */
public class RewriterTan extends Rewriter {

	public RewriterTan() { }
	
	@Override
	protected void rewriteExpression(Expression x) 
	throws NoResultException {
		final Operator operator = x.getOperator();
		if (operator == Operator.DIV) {
			final Primitive first = x.getFirstOperand();
			final Primitive second = x.getSecondOperand();
			if (first instanceof FunctionApplication && second instanceof FunctionApplication) {
				final FunctionApplication firstFA = (FunctionApplication) first;
				final FunctionApplication secondFA = (FunctionApplication) second;
				if (firstFA.getOperator().equals(FunctionApplication.SIN) &&
					secondFA.getOperator().equals(FunctionApplication.COS) &&
					firstFA.getArgs()[0].equals(secondFA.getArgs()[0])) {
					try {
						setResult(calc.applyFunction(x.getType(), FunctionApplication.TAN, firstFA.getArgs()[0]));
					} catch (InvalidOperandException | InvalidTypeException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
					return;
				}
			}
		}
		super.rewriteExpression(x);
	}
}
