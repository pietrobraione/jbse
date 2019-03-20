package jbse.rewr;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites {@code A / B == 0} to {@code A == 0 && B != 0}, 
 * and {@code A / B != 0} to {@code A != 0}, if {@code A} and
 * {@code B} have floating type. If {@code A} and {@code B}
 * have integral type rewrites {@code A / B == 0} to 
 * {@code abs(A) < abs(B) && B != 0}, 
 * and {@code A / B != 0} to {@code abs(A) >= abs(B)}
 * 
 * @author Pietro Braione
 *
 */
public class RewriterDivisionEqualsZero extends RewriterCalculatorRewriting {
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
						if (Type.isPrimitiveIntegral(first.getType())) {
							if (op == Operator.EQ) { 
								setResult(rewrite(this.calc.applyFunctionPrimitive(x.getType(), x.historyPoint(), (x.getType() == Type.LONG ? PrimitiveSymbolicApply.ABS_LONG : PrimitiveSymbolicApply.ABS_INT), A)
												 .lt(this.calc.applyFunctionPrimitive(x.getType(), x.historyPoint(), (x.getType() == Type.LONG ? PrimitiveSymbolicApply.ABS_LONG : PrimitiveSymbolicApply.ABS_INT), B).pop()).and(this.calc.push(B).eq(right).not().pop()).pop()));
							} else {
								setResult(rewrite(this.calc.applyFunctionPrimitive(x.getType(), x.historyPoint(), (x.getType() == Type.LONG ? PrimitiveSymbolicApply.ABS_LONG : PrimitiveSymbolicApply.ABS_INT), A)
										 .lt(this.calc.applyFunctionPrimitive(x.getType(), x.historyPoint(), (x.getType() == Type.LONG ? PrimitiveSymbolicApply.ABS_LONG : PrimitiveSymbolicApply.ABS_INT), B).pop()).pop()));
							}
						} else {
							if (op == Operator.EQ) { 
								setResult(rewrite(this.calc.push(A).eq(right).and(this.calc.push(B).eq(right).not().pop()).pop()));
							} else {
								setResult(rewrite(this.calc.push(A).ne(right).pop()));
							}
						}
					} catch (InvalidInputException | InvalidOperandException | InvalidTypeException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
					return;
				}
			}
		}
		//in all other cases, default
		setResult(x);
		
		/* TODO as implemented now does not work always, since 
		 * 
		 * ! (A / B == 0) -> ! (A == 0 && B != 0) -> A != 0 || B == 0
		 * 
		 * and it should be instead reduced to A != 0 && B != 0. Luckily normalization  
		 * rewrites ! (A == B) to A != B. Either remove the ... && B != 0 clause
		 * or gather all of them and append them at the end.
		 */
	}
}
