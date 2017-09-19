package jbse.rewr;

import jbse.rewr.exc.NoResultException;
import jbse.val.Expression;
import jbse.val.Operator;

/**
 * A rewriter which normalizes an expression. It assumes that its input
 * is in the form produced by {@link RewriterPolynomials}. 
 * 
 * @author Pietro Braione
 */
public class RewriterNormalize extends Rewriter {
	public RewriterNormalize() { }

	@Override
	protected void rewriteExpression(Expression x) 
	throws NoResultException {
		final Operator operator = x.getOperator();
		if (operator == Operator.MUL || operator == Operator.ADD) {
			setResult(Polynomial.of(this.calc, x).toPrimitiveNormalized());
		} else {
			super.rewriteExpression(x);
		}
	}
}
