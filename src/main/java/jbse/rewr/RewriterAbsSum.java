package jbse.rewr;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicAtomic;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveVisitor;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites comparisons of {@code A +/- abs(A)} with {@code 0}.
 * Requires {@link RewriterOperationOnSimplex} and {@link RewriterPolynomials}.
 * 
 * @author Pietro Braione
 */
public class RewriterAbsSum extends RewriterCalculatorRewriting {
	public RewriterAbsSum() { }

	@Override
	protected void rewriteExpression(Expression x) 
	throws NoResultException {
		//checks for a comparison expression
		Operator operator = x.getOperator();
		if (operator != Operator.EQ && operator != Operator.NE &&
			operator != Operator.GT && operator != Operator.LE &&
			operator != Operator.LT && operator != Operator.GE) {
			setResult(x);
			return;
		}

		//checks for a comparison with zero
		final Primitive first = x.getFirstOperand();
		final Primitive second = x.getSecondOperand();
		final Expression subExpr;
		if (first instanceof Expression && second instanceof Simplex && ((Simplex) second).isZeroOne(true)) {
			subExpr = (Expression) first;
		} else if (second instanceof Expression && first instanceof Simplex && ((Simplex) first).isZeroOne(true)) {
			subExpr = (Expression) second;
			operator = operator.twist();
		} else {
			setResult(x);
			return;
		}

		//reduces the term of comparison to a polynomial and 
		//searches an abs(X) monomial in it
		final Polynomial subExprPolynomial = Polynomial.of(this.calc, subExpr);
		final PrimitiveSymbolicApply abs = findAbs(subExprPolynomial.toPrimitive(this.calc));
		if (abs == null) {
			setResult(x);
			return;
		}

		//checks that the multiplier of abs(X) is 1 or -1
		try {
			final char absType = abs.getType();
			final Primitive minusOne = this.calc.pushInt(-1).to(absType).pop();
			final Primitive absArg = (Primitive) abs.getArgs()[0];
			final Simplex absMultiplier = subExprPolynomial.getMultiplier(this.calc, Monomial.of(this.calc, abs));
			final boolean absNegated;
			final Primitive toRemove;
			if (absMultiplier.isZeroOne(false)) {
				absNegated = false;
				toRemove = this.calc.push(abs).mul(minusOne).pop();
			} else if (((Simplex) this.calc.push(absMultiplier).mul(minusOne).pop()).isZeroOne(false)) {
				absNegated = true;
				toRemove = abs;
			} else {
				setResult(x);
				return;
			}

			//checks that what remains by taking out abs(X) is X or -X
			//and in the case elaborates the result
			final Polynomial subOtherPolynomial;
			subOtherPolynomial = subExprPolynomial.add(this.calc, Polynomial.of(this.calc, toRemove));
			final Primitive subOther = subOtherPolynomial.toPrimitive(this.calc);
			if (subOtherPolynomial.equals(Polynomial.of(this.calc, absArg))) {
				processAbs(absNegated, absArg, false, subOther, operator);
			} else if (subOtherPolynomial.equals(Polynomial.of(this.calc, this.calc.push(absArg).mul(minusOne).pop()))) {
				processAbs(absNegated, absArg, true, subOther, operator);
			} else {
				setResult(x);
				return;
			}			
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	private PrimitiveSymbolicApply findAbs(Primitive x) {
		final AbsPrimitiveVisitor absVisitor = new AbsPrimitiveVisitor();
		try {
			x.accept(absVisitor);
		} catch (Exception e) {
			//will not happen
			throw new AssertionError();
		}
		return absVisitor.abs;
	}

	private static class AbsPrimitiveVisitor implements PrimitiveVisitor {
		PrimitiveSymbolicApply abs = null;

		@Override
		public void visitAny(Any x) { }

		@Override
		public void visitExpression(Expression e) throws Exception {
			final Operator op = e.getOperator();
			if (op == Operator.ADD || op == Operator.MUL) {
				e.getFirstOperand().accept(this);
				e.getSecondOperand().accept(this);
			}
		}

		@Override
		public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) {
			if (x.getOperator().equals(PrimitiveSymbolicApply.ABS_DOUBLE) || 
				x.getOperator().equals(PrimitiveSymbolicApply.ABS_FLOAT) || 
				x.getOperator().equals(PrimitiveSymbolicApply.ABS_INT) ||
				x.getOperator().equals(PrimitiveSymbolicApply.ABS_LONG)) {
				this.abs = x;
			}
		}

		@Override
		public void visitWideningConversion(WideningConversion x) { }

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) { }

		@Override
		public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) { }

		@Override
		public void visitSimplex(Simplex x) throws Exception { }

		@Override
		public void visitTerm(Term x) throws Exception { }
	}

	private void processAbs(boolean absNegated, Primitive absArg, boolean otherNegated, Primitive subOther, Operator operator) 
	throws NoResultException {
		try {
			final Primitive zero = this.calc.pushInt(0).to(absArg.getType()).pop();
			switch (operator) {
			case EQ:
				if (absNegated == otherNegated) {
					//|A| + A == 0 iff -|A| - A == 0 iff A <= 0
					setResult(this.calc.push(absArg).le(zero).pop());
				} else {
					//|A| - A == 0 iff -|A| + A == 0 iff A >= 0
					setResult(this.calc.push(absArg).ge(zero).pop());
				}
				break;
			case NE:
				if (absNegated == otherNegated) {
					//|A| + A != 0 iff -|A| - A == 0 iff A > 0
					setResult(this.calc.push(absArg).gt(zero).pop());
				} else {
					//|A| - A != 0 iff -|A| + |A| != 0 iff A < 0
					setResult(this.calc.push(absArg).lt(zero).pop());
				}
				break;
			case GT:
				if (!absNegated && !otherNegated) {
					//|A| + A > 0 iff A > 0
					setResult(this.calc.push(absArg).gt(zero).pop());
				} else if (!absNegated && otherNegated) {
					//|A| - A > 0 iff A < 0
					setResult(this.calc.push(absArg).lt(zero).pop());
				} else {
					//-|A| + A > 0 never, -|A| - A > 0 never
					setResult(this.calc.valBoolean(false));
				}
				break;
			case LE:
				if (!absNegated && !otherNegated) {
					//|A| + A <= 0 iff A <= 0
					setResult(this.calc.push(absArg).le(zero).pop());
				} else if (!absNegated && otherNegated) {
					//|A| - A <= 0 iff A >= 0
					setResult(this.calc.push(absArg).ge(zero).pop());
				} else {
					//-|A| + A <= 0 always, -|A| - A <= 0 always
					setResult(this.calc.valBoolean(true));
				}
				break;
			case LT:
				if (absNegated && !otherNegated) {
					//-|A| + A < 0 iff A < 0
					setResult(this.calc.push(absArg).lt(zero).pop());
				} else if (absNegated && otherNegated) {
					//-|A| - A < 0 iff A > 0
					setResult(this.calc.push(absArg).gt(zero).pop());
				} else {
					//|A| + A < 0 never, |A| - A < 0 never
					setResult(this.calc.valBoolean(false));
				} 
				break;
			case GE:
				if (absNegated && !otherNegated) {
					//-|A| + A >= 0 iff A >= 0
					setResult(this.calc.push(absArg).ge(zero).pop());
				} else if (absNegated && otherNegated) {
					//-|A| - A >= 0 iff A <= 0
					setResult(this.calc.push(absArg).le(zero).pop());
				} else {
					//|A| + A >= 0 always, |A| - A >= 0 always
					setResult(this.calc.valBoolean(true));
				}
				break;
			default:
				//this should never happen
				throw new UnexpectedInternalException("Unreachable branch");
			}
		} catch (InvalidTypeException | InvalidOperandException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
