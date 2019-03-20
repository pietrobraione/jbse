package jbse.rewr;

import static jbse.common.Type.isPrimitiveIntegral;

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
				setResult(x);
				return;
			}

			//gets the two operands, considering the case operator == NEG 
			//as it were a binary multiplication
			final Primitive firstOperand, secondOperand;
			if (operator == Operator.NEG) {
				firstOperand = this.calc.pushInt(-1).to(x.getOperand().getType()).pop();
				secondOperand = x.getOperand();
			} else {
				firstOperand = x.getFirstOperand();
				secondOperand = x.getSecondOperand();
			}

			//splits the first fraction into its numerator/denominator
			final boolean firstOperandIsAFraction;
			final Primitive firstNumer;
			final Primitive firstDenom;
			if (firstOperand instanceof Expression) {
				final Expression firstOperandExpression = (Expression) firstOperand;
				final Operator firstOperandOperator = firstOperandExpression.getOperator();
				if (firstOperandOperator == Operator.DIV) {
					firstOperandIsAFraction = true;
					firstNumer = firstOperandExpression.getFirstOperand();
					firstDenom = firstOperandExpression.getSecondOperand();
				} else {
					firstOperandIsAFraction = false;
					firstNumer = firstOperand;
					firstDenom = this.calc.pushInt(1).to(firstOperand.getType()).pop();
				}
			} else {
				firstOperandIsAFraction = false;
				firstNumer = firstOperand;
				firstDenom = this.calc.pushInt(1).to(firstOperand.getType()).pop();
			}

			//splits the second fraction into its numerator/denominator, reducing
			//the cases operator == SUB and (operator == DIV or operator == NEG) 
			//to the cases operator == ADD and operator == MUL respectively
			final boolean secondOperandIsAFraction;
			final Primitive secondNumer;
			final Primitive secondDenom;
			if (secondOperand instanceof Expression) {
				final Expression secondOperandExpression = (Expression) secondOperand;
				final Operator secondOperandOperator = secondOperandExpression.getOperator();
				if (secondOperandOperator == Operator.DIV && operator == Operator.DIV) {
					secondOperandIsAFraction = true;
					secondNumer = secondOperandExpression.getSecondOperand();
					secondDenom = secondOperandExpression.getFirstOperand();
				} else if (secondOperandOperator == Operator.DIV) {
					secondOperandIsAFraction = true;
					secondNumer = (operator == Operator.SUB ? 
								   this.calc.push(secondOperandExpression.getFirstOperand()).neg().pop() : 
								   secondOperandExpression.getFirstOperand());
					secondDenom = secondOperandExpression.getSecondOperand();
				} else if (operator == Operator.DIV) {
					secondOperandIsAFraction = false;
					secondNumer = this.calc.pushInt(1).to(secondOperand.getType()).pop();
					secondDenom = secondOperand;
				} else {
					secondOperandIsAFraction = false;
					secondNumer = secondOperand;
					secondDenom = this.calc.pushInt(operator == Operator.SUB ? -1 : 1).to(secondOperand.getType()).pop();
				}
			} else if (operator == Operator.DIV) {
				secondOperandIsAFraction = false;
				secondNumer = this.calc.pushInt(1).to(secondOperand.getType()).pop();
				secondDenom = secondOperand;
			} else {
				secondOperandIsAFraction = false;
				secondNumer = secondOperand;
				secondDenom = this.calc.pushInt(operator == Operator.SUB ? -1 : 1).to(secondOperand.getType()).pop();
			}
			
			//in the case the expression has integral type all divisions are
			//integer divisions, for which the distributive property does not hold
			//unless the dividend is a multiple of the divisor. To make the 
			//distributive property hold, subtracts the remainder from the 
			//numerators to make them multiples of the
			//denominator, exploiting the fact that a / b == (a - a % b) / b.
			//Note that this must *not* be done in the cases where the two operands
			//are not fractions and in the case (a / b) / c, for which the
			//equality (a / b) / c == a / (b * c) holds.
			final Primitive firstNumerDistrib, secondNumerDistrib, firstDenomDistrib, secondDenomDistrib;
			if (isPrimitiveIntegral(x.getType())) {
				if (operator == Operator.NEG) {
					//nothing to do
					firstNumerDistrib = firstNumer;
					firstDenomDistrib = firstDenom;
					secondNumerDistrib = secondNumer;
					secondDenomDistrib = secondDenom;
				} else if (firstOperandIsAFraction && !secondOperandIsAFraction && operator == Operator.DIV) {
					//nothing to do
					firstNumerDistrib = firstNumer;
					firstDenomDistrib = firstDenom;
					secondNumerDistrib = secondNumer;
					secondDenomDistrib = secondDenom;
				} else if ((firstOperandIsAFraction || secondOperandIsAFraction) && operator == Operator.DIV) {
					firstNumerDistrib = this.calc.push(firstNumer).sub(this.calc.push(firstNumer).rem(firstDenom).pop()).pop();
					firstDenomDistrib = firstDenom;
					secondNumerDistrib = secondNumer; 
					secondDenomDistrib = this.calc.push(secondDenom).sub(this.calc.push(secondDenom).rem(secondNumer).pop()).pop();
				} else if (firstOperandIsAFraction || secondOperandIsAFraction) {
					firstNumerDistrib = this.calc.push(firstNumer).sub(this.calc.push(firstNumer).rem(firstDenom).pop()).pop();
					firstDenomDistrib = firstDenom;
					secondNumerDistrib = this.calc.push(secondNumer).sub(this.calc.push(secondNumer).rem(secondDenom).pop()).pop();
					secondDenomDistrib = secondDenom;
				} else {
					//nothing to do
					firstNumerDistrib = firstNumer;
					firstDenomDistrib = firstDenom;
					secondNumerDistrib = secondNumer;
					secondDenomDistrib = secondDenom;
				}
			} else {
				firstNumerDistrib = firstNumer;
				firstDenomDistrib = firstDenom;
				secondNumerDistrib = secondNumer;
				secondDenomDistrib = secondDenom;
			}
			
			//builds the result numerator and denominator as polynomials
			final Polynomial firstNumerPoly = Polynomial.of(this.calc, firstNumerDistrib);
			final Polynomial firstDenomPoly = Polynomial.of(this.calc, firstDenomDistrib);
			final Polynomial secondNumerPoly = Polynomial.of(this.calc, secondNumerDistrib);
			final Polynomial secondDenomPoly = Polynomial.of(this.calc, secondDenomDistrib);
			final Polynomial[] resultDivPoly;
			if (operator == Operator.MUL || operator == Operator.DIV || operator == Operator.NEG) {
				//product
				resultDivPoly = firstNumerPoly.mul(this.calc, secondNumerPoly).div(this.calc, firstDenomPoly.mul(this.calc, secondDenomPoly));
			} else {
				//sum
				resultDivPoly = firstNumerPoly.mul(this.calc, secondDenomPoly).add(this.calc, secondNumerPoly.mul(this.calc, firstDenomPoly)).div(this.calc, firstDenomPoly.mul(this.calc, secondDenomPoly));
			}

			//converts the result to Primitive and sets it
			final Primitive resultNumer = resultDivPoly[0].toPrimitive(this.calc);
			if (resultDivPoly[1] == null) {
				setResult(resultNumer);
			} else {
				final Primitive resultDenom = resultDivPoly[1].toPrimitive(this.calc);
				setResult(Expression.makeExpressionBinary(resultNumer, Operator.DIV, resultDenom));
			}
		} catch (InvalidOperatorException | InvalidOperandException | 
				 InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
