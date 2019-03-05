package jbse.rewr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites {@code A * sin(X) * sin(X) + A * cos(X) * cos(X)} to {@code A}
 * under all subexpressions it can find.
 * 
 * @author Pietro Braione
 *
 */
public class RewriterSinCos extends RewriterCalculatorRewriting {
	public RewriterSinCos() { }

	private List<Monomial> getMonomialsWithSinCos(Polynomial poly) {
		final ArrayList<Monomial> retVal = new ArrayList<Monomial>();
		for (Monomial m : poly.representation().keySet()) {
			for (Primitive p : m.representation().keySet()) {
				if (p instanceof PrimitiveSymbolicApply) {
					final PrimitiveSymbolicApply pF = (PrimitiveSymbolicApply) p;
					if (pF.getOperator().equals(PrimitiveSymbolicApply.SIN) ||
						pF.getOperator().equals(PrimitiveSymbolicApply.COS)) {
						try {
							retVal.add(m.mul(this.calc, Monomial.of(this.calc, poly.representation().get(m))));
						} catch (InvalidTypeException e) {
							//this should never happen
							throw new UnexpectedInternalException(e);
						}
						break;
					}
				}
			}
		}
		return retVal;
	}

	private Monomial checkSinCos(Monomial first, Monomial second) 
	throws InvalidTypeException {
		try {
			if (this.calc.push(first.getMultiplier()).eq(second.getMultiplier()).pop().surelyTrue()) {
				//divides the monomials
				final Monomial[] div = first.div(this.calc, second);
				final Primitive firstDiv = div[0].createBase(this.calc).toPrimitive();
				final Primitive secondDiv = div[1].createBase(this.calc).toPrimitive();

				//checks that the result is sin(X) * sin(X) / (cos(X) * cos(X)) or the inverse
				if (firstDiv instanceof Expression && secondDiv instanceof Expression) {
					final Expression firstDivExp = (Expression) firstDiv;
					final Operator firstDivOperator = firstDivExp.getOperator();
					final Expression secondDivExp = (Expression) secondDiv;
					final Operator secondDivOperator = secondDivExp.getOperator();
					if (firstDivOperator == Operator.MUL && secondDivOperator == Operator.MUL) {
						final Primitive firstDivFirstOperand = firstDivExp.getFirstOperand();
						final Primitive firstDivSecondOperand = firstDivExp.getSecondOperand();
						final Primitive secondDivFirstOperand = secondDivExp.getFirstOperand();
						final Primitive secondDivSecondOperand = secondDivExp.getSecondOperand();
						if (firstDivFirstOperand.equals(firstDivSecondOperand) &&
								secondDivFirstOperand.equals(secondDivSecondOperand) &&
								firstDivFirstOperand  instanceof PrimitiveSymbolicApply && 
								secondDivFirstOperand instanceof PrimitiveSymbolicApply) {
							PrimitiveSymbolicApply f0 = (PrimitiveSymbolicApply) firstDivFirstOperand;
							PrimitiveSymbolicApply f1 = (PrimitiveSymbolicApply) secondDivFirstOperand;
							if ((f0.getOperator().equals(PrimitiveSymbolicApply.SIN) && f1.getOperator().equals(PrimitiveSymbolicApply.COS)) ||
									(f0.getOperator().equals(PrimitiveSymbolicApply.COS) && f1.getOperator().equals(PrimitiveSymbolicApply.SIN))) {
								if (f0.getArgs()[0].equals(f1.getArgs()[0])) {
									return div[0].createBase(this.calc);
								}
							}
						}
					}
				}
			}
		} catch (InvalidOperandException e) {
			//should never happen because the caller already checked the parameters
			throw new UnexpectedInternalException(e);
		}
		return null;
	}

	private static class SinCosPair {
		public SinCosPair(Monomial mFirst, Monomial mSecond, Monomial mFirstDiv) {
			this.mFirst = mFirst;
			this.mSecond = mSecond;
			this.mFirstDiv = mFirstDiv;
		}

		Monomial mFirst;
		Monomial mSecond;
		Monomial mFirstDiv;
	}

	@Override
	protected void rewriteExpression(Expression x) 
	throws NoResultException {
		final Operator operator = x.getOperator();
		if (operator == Operator.ADD || operator == Operator.MUL) {
			final Polynomial poly = Polynomial.of(this.calc, x);
			final List<Monomial> monomials = getMonomialsWithSinCos(poly);			
			final Set<SinCosPair> pairs = new HashSet<SinCosPair>();
		mainloop:
			for (final ListIterator<Monomial> iFirst = monomials.listIterator(); iFirst.hasNext(); ) {
				int nextIndex = iFirst.nextIndex();
				final Monomial mFirst = iFirst.next();
				if (iFirst.hasNext()) { //skips one
					for (final ListIterator<Monomial> iSecond = monomials.listIterator(nextIndex + 1); iSecond.hasNext(); ) {
						final Monomial mSecond = iSecond.next();
						final Monomial mFirstDiv;
						try {
							mFirstDiv = checkSinCos(mFirst, mSecond);
						} catch (InvalidTypeException e) {
							//this should never happen
							throw new UnexpectedInternalException(e);
						}
						if (mFirstDiv != null) {
							pairs.add(new SinCosPair(mFirst, mSecond, mFirstDiv));
							continue mainloop;
						}
					}
				}
			}

			if (pairs.size() > 0) {
				Primitive result = x;
				try {
					for (SinCosPair p : pairs) {
						result = this.calc.push(result).add(this.calc.push(p.mFirst.toPrimitive()).neg().pop()).pop();
						result = this.calc.push(result).add(this.calc.push(p.mSecond.toPrimitive()).neg().pop()).pop();
						result = this.calc.push(result).add(this.calc.push(p.mFirst.toPrimitive()).div(p.mFirstDiv.toPrimitive()).pop()).pop();
					}
					setResult(this.calc.push(result).to(x.getType()).pop());
				} catch (InvalidTypeException | InvalidOperandException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
				return;
			}
		}

		//all other cases
		setResult(x);
	}
}
