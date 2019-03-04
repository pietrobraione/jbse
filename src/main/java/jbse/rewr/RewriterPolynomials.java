package jbse.rewr;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites the sum, subtraction, product or division of ratios of 
 * polynomials by expanding them. 
 * 
 * @author Pietro Braione
 *
 */
public class RewriterPolynomials extends RewriterCalculatorRewriting {
	public RewriterPolynomials() { }

	@Override
	protected void rewriteExpression(Expression x) throws NoResultException {
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
				firstFraction = this.calc.pushInt(-1).to(x.getOperand().getType()).pop();
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
					firstNumer = Polynomial.of(this.calc, firstOperandExpression.getFirstOperand());
					firstDenom = Polynomial.of(this.calc, firstOperandExpression.getSecondOperand());
				} else {
					firstNumer = Polynomial.of(this.calc, firstFraction);
					firstDenom = Polynomial.of(this.calc, this.calc.pushInt(1).to(firstFraction.getType()).pop());
				}
			} else {
				firstNumer = Polynomial.of(this.calc, firstFraction);
				firstDenom = Polynomial.of(this.calc, this.calc.pushInt(1).to(firstFraction.getType()).pop());
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
					secondNumer = Polynomial.of(this.calc, secondOperandExpression.getSecondOperand());
					secondDenom = Polynomial.of(this.calc, secondOperandExpression.getFirstOperand());
				} else if (secondOperandOperator == Operator.DIV) {
					secondNumer = (operator == Operator.SUB ? 
							Polynomial.of(this.calc, secondOperandExpression.getFirstOperand()).neg(this.calc) : 
								Polynomial.of(this.calc, secondOperandExpression.getFirstOperand()));
					secondDenom = Polynomial.of(this.calc, secondOperandExpression.getSecondOperand());
				} else if (operator == Operator.DIV) {
					secondNumer = Polynomial.of(this.calc, this.calc.pushInt(1).to(secondFraction.getType()).pop());
					secondDenom = Polynomial.of(this.calc, secondFraction);
				} else {
					secondNumer = Polynomial.of(this.calc, secondFraction);
					secondDenom = Polynomial.of(this.calc, this.calc.pushInt(operator == Operator.SUB ? -1 : 1).to(secondFraction.getType()).pop());
				}
			} else if (operator == Operator.DIV) {
				secondNumer = Polynomial.of(this.calc, this.calc.pushInt(1).to(secondFraction.getType()).pop());
				secondDenom = Polynomial.of(this.calc, secondFraction);
			} else {
				secondNumer = Polynomial.of(this.calc, secondFraction);
				secondDenom = Polynomial.of(this.calc, this.calc.pushInt(operator == Operator.SUB ? -1 : 1).to(secondFraction.getType()).pop());
			}

			//builds the result numerator and denominator as polynomials
			final Polynomial[] resultDiv;
			if (operator == Operator.MUL || operator == Operator.DIV || operator == Operator.NEG) {
				//product
				resultDiv = firstNumer.mul(this.calc, secondNumer).div(this.calc, firstDenom.mul(this.calc, secondDenom));
			} else {
				//sum
				resultDiv = firstNumer.mul(this.calc, secondDenom).add(this.calc, secondNumer.mul(this.calc, firstDenom)).div(this.calc, firstDenom.mul(this.calc, secondDenom));
			}

			//converts the result to Primitive and sets it
			final Primitive resultNumerator = resultDiv[0].toPrimitive(this.calc);
			if (resultDiv[1] == null) {
				setResult(resultNumerator);
			} else {
				final Primitive resultDenominator = resultDiv[1].toPrimitive(this.calc);
				setResult(Expression.makeExpressionBinary(resultNumerator, Operator.DIV, resultDenominator));
			}
		} catch (InvalidOperatorException | InvalidOperandException | 
				 InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
