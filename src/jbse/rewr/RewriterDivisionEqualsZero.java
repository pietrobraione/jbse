package jbse.rewr;

import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.exc.NoResultException;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Rewrites {@code A / B == 0} to {@code A == 0 && B != 0}, 
 * and {@code A / B != 0} to {@code A != 0}.
 * 
 * @author Pietro Braione
 *
 */
public class RewriterDivisionEqualsZero extends Rewriter {
	public RewriterDivisionEqualsZero() { }

	@Override
	protected void rewriteExpression(Expression x) 
	throws NoResultException {
		final Operator op = x.getOperator();
		if (op == Operator.EQ || op == Operator.NE) {
			final Primitive first = x.getFirstOperand();
			final Primitive second = x.getSecondOperand();
			if ((first instanceof Simplex || second instanceof Simplex) &&
					(first instanceof Expression || second instanceof Expression)) {
				final Expression left = (Expression) (first instanceof Expression ? first : second);
				final Simplex right = (Simplex) (first instanceof Simplex ? first : second);
				if (left.getOperator().equals(Operator.DIV) && right.isZeroOne(true)) {
					final Primitive A = left.getFirstOperand();
					final Primitive B = left.getSecondOperand();
					try {
						if (op == Operator.EQ) { 
							setResult(rewrite(A.eq(right).and(B.eq(right).not())));
						} else {
							setResult(rewrite(A.ne(right)));
						}
					} catch (InvalidOperandException | InvalidTypeException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
					return;
				}
			}
		}
		//in all other cases, default
		super.rewriteExpression(x);
		
		/* TODO as implemented now does not work always, since 
		 * 
		 * ! (A / B == 0) -> ! (A == 0 && B != 0) -> A != 0 || B == 0
		 * 
		 * and it should be instead reduced to A != 0 && B != 0. Luckily normalization  
		 * rewrites ! (A == B) to A != B. Either remove the ... && B != 0 clause
		 * or cumulate all of them and append at the end.
		 */
	}
}
