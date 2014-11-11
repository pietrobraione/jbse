package jbse.rewr;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidOperatorException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.rewr.NoResultException;
import jbse.mem.Expression;
import jbse.mem.Operator;
import jbse.mem.Primitive;

/**
 * Rewrites the sum, subtraction, product or division of ratios of 
 * polynomials by expanding them. 
 * 
 * @author Pietro Braione
 *
 */
public class RewriterPolynomials extends Rewriter {
	public RewriterPolynomials() { }

	@Override
	protected void rewriteExpression(Expression x) throws NoResultException, UnexpectedInternalException {
		try {
			//detects trivial cases
			final Operator operator = x.getOperator();
			if (operator != Operator.ADD && operator != Operator.SUB && operator != Operator.MUL && operator != Operator.DIV && operator != Operator.NEG) {
				super.rewriteExpression(x);
				return;
			}

			//builds the two fractions, handling the case operator == NEG
			final Primitive firstFraction, secondFraction;
			if (operator == Operator.NEG) {
				firstFraction = calc.valInt(-1).to(x.getOperand().getType());
				secondFraction = x.getOperand();
			} else {
				firstFraction = x.getFirstOperand();
				secondFraction = x.getSecondOperand();
			}

			//splits the first fraction into its numerator/denominator
			final Polynomial firstNumer;
			final Polynomial firstDenom;
			if (firstFraction instanceof Expression) {
				final Expression firstOperandExpression = (Expression) firstFraction;
				final Operator firstOperandOperator = firstOperandExpression.getOperator();
				if (firstOperandOperator == Operator.DIV) {
					firstNumer = Polynomial.of(calc, firstOperandExpression.getFirstOperand());
					firstDenom = Polynomial.of(calc, firstOperandExpression.getSecondOperand());
				} else {
					firstNumer = Polynomial.of(calc, firstFraction);
					firstDenom = Polynomial.of(calc, calc.valInt(1).to(firstFraction.getType()));
				}
			} else {
				firstNumer = Polynomial.of(calc, firstFraction);
				firstDenom = Polynomial.of(calc, calc.valInt(1).to(firstFraction.getType()));
			}

			//splits the second fraction into its numerator/denominator, reducing
			//the cases operator == SUB and (operator == DIV or operator == NEG) 
			//to the cases operator == ADD and operator == MUL respectively
			final Polynomial secondNumer;
			final Polynomial secondDenom;
			if (secondFraction instanceof Expression) {
				final Expression secondOperandExpression = (Expression) secondFraction;
				final Operator secondOperandOperator = secondOperandExpression.getOperator();
				if (secondOperandOperator == Operator.DIV && operator == Operator.DIV) {
					secondNumer = Polynomial.of(calc, secondOperandExpression.getSecondOperand());
					secondDenom = Polynomial.of(calc, secondOperandExpression.getFirstOperand());
				} else if (secondOperandOperator == Operator.DIV) {
					secondNumer = (operator == Operator.SUB ? 
							Polynomial.of(calc, secondOperandExpression.getFirstOperand()).neg() : 
								Polynomial.of(calc, secondOperandExpression.getFirstOperand()));
					secondDenom = Polynomial.of(calc, secondOperandExpression.getSecondOperand());
				} else if (operator == Operator.DIV) {
					secondNumer = Polynomial.of(calc, calc.valInt(1).to(secondFraction.getType()));
					secondDenom = Polynomial.of(calc, secondFraction);
				} else {
					secondNumer = Polynomial.of(calc, secondFraction);
					secondDenom = Polynomial.of(calc, calc.valInt(operator == Operator.SUB ? -1 : 1).to(secondFraction.getType()));
				}
			} else if (operator == Operator.DIV) {
				secondNumer = Polynomial.of(calc, calc.valInt(1).to(secondFraction.getType()));
				secondDenom = Polynomial.of(calc, secondFraction);
			} else {
				secondNumer = Polynomial.of(calc, secondFraction);
				secondDenom = Polynomial.of(calc, calc.valInt(operator == Operator.SUB ? -1 : 1).to(secondFraction.getType()));
			}

			//builds the result numerator and denominator as polynomials
			final Polynomial[] resultDiv;
			if (operator == Operator.MUL || operator == Operator.DIV || operator == Operator.NEG) {
				//product
				resultDiv = firstNumer.mul(secondNumer).div(firstDenom.mul(secondDenom));
			} else {
				//sum
				resultDiv = firstNumer.mul(secondDenom).add(secondNumer.mul(firstDenom)).div(firstDenom.mul(secondDenom));
			}

			//converts the result to Primitive and sets it
			final Primitive resultNumerator = resultDiv[0].toPrimitive();
			if (resultDiv[1] == null) {
				setResult(resultNumerator);
			} else {
				final Primitive resultDenominator = resultDiv[1].toPrimitive();
				setResult(Expression.makeExpressionBinary(this.calc, resultNumerator, Operator.DIV, resultDenominator));
			}
		} catch (InvalidOperatorException | InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
