package jbse.rewr;

import java.util.NoSuchElementException;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Expression;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites all the {@link Expression}s or {@link NarrowingConversion}s 
 * with zero/unit operations, plus some trivial simplifications. The list
 * of applied rewritings are: 
 * <ul>
 * <li> x && x -> x, x || x -> x</li> 
 * <li>x && true -> x, x && false -> false, true && x -> x, false && x -> false</li>
 * <li>x + 0 -> x, 0 + x -> x</li>
 * <li>x - 0 -> x, 0 - x -> -x</li>
 * <li>x * 0 -> 0, 0 * x -> 0</li>
 * <li>x * 1 -> x, 1 * x -> x</li>
 * <li>0 / x -> 0, x / 1 -> x</li>
 * <li>0 % x -> 0, x % 1 -> 0, x % (-1) -> 0</li>
 * <li>(x / n1) / n2 -> x / (n1 * n2)</li>
 * <li>NARROW-T2(WIDEN-T1(x)) -> x</li>
 * </ul>
 * 
 * @author Pietro Braione
 */
public final class RewriterZeroUnit extends RewriterCalculatorRewriting {
    public RewriterZeroUnit() { }

    @Override
    protected void rewriteExpression(Expression x) 
    throws NoResultException {        
        final boolean unary = x.isUnary();
        final Operator operator = x.getOperator();

        //gets operands
        final Primitive firstOperand, secondOperand;
        if (unary) {
            firstOperand = x.getOperand();
            secondOperand = null;
        } else {
            firstOperand = x.getFirstOperand();
            secondOperand = x.getSecondOperand();
        }

        if (operator == Operator.AND && firstOperand.equals(secondOperand)) {
            //x && x -> x
        	setResult(x.getFirstOperand());
        } else if (operator == Operator.OR && firstOperand.equals(secondOperand)) {
            //x || x -> x
        	setResult(x.getFirstOperand());
        } else if (!unary && (firstOperand instanceof Simplex || secondOperand instanceof Simplex)) {
            //binary operation with one Simplex operand
            attemptRewriteBinaryWithSimplex(x);
        } else {
        	//none of the above cases
        	setResult(x);
        }
    }

    @Override
    protected void rewriteNarrowingConversion(NarrowingConversion x)
    throws NoResultException {
        final Primitive arg = x.getArg();
        
        if (arg instanceof WideningConversion) {
        	final Primitive wideningArg = ((WideningConversion) arg).getArg();
        	if (x.getType() == wideningArg.getType()) {
                //NARROW-T2(WIDEN-T1(x)) -> x
        		setResult(wideningArg);
        	} else {
        		setResult(x);
        	}
        } else {
        	setResult(x);
        }
    }
    
    private void attemptRewriteBinaryWithSimplex(Expression x) throws NoResultException {
        final Operator operator = x.getOperator();
    	final Primitive firstOperand = x.getFirstOperand();
    	final Primitive secondOperand = x.getSecondOperand();
        final boolean firstIsSimplex = (firstOperand instanceof Simplex);
        final Simplex simplexOperand = (Simplex) (firstIsSimplex ? firstOperand : secondOperand);
        final Primitive otherOperand = (firstIsSimplex ? secondOperand : firstOperand);
        
		try {
			if (operator == Operator.AND) {
				//x && true -> x, x && false -> false, true && x -> x, false && x -> false
				if ((Boolean) simplexOperand.getActualValue()) {
					setResult(otherOperand);
				} else {
					setResult(simplexOperand);
				}
			} else if (operator == Operator.OR) {
				//x || true -> true, x || false -> x, true || x -> true, false || x -> x
				if ((Boolean) simplexOperand.getActualValue()) {
					setResult(simplexOperand);
				} else {
					setResult(otherOperand);
				}
			} else if (operator == Operator.ADD && simplexOperand.isZeroOne(true)) {
				//x + 0 -> x, 0 + x -> x
				setResult(otherOperand);
			} else if (operator == Operator.SUB && simplexOperand.isZeroOne(true)) {
				//x - 0 -> x, 0 - x -> -x
				if (firstIsSimplex) {
					try {
						setResult(this.calc.push(otherOperand).neg().pop());
					} catch (InvalidTypeException e) {
						//does nothing, falls through
					} catch (InvalidOperandException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
				} else {
					setResult(otherOperand);
				}
			} else if (operator == Operator.MUL && simplexOperand.isZeroOne(true)) {
				//x * 0 -> 0, 0 * x -> 0
				setResult(simplexOperand);
			} else if (operator == Operator.MUL && simplexOperand.isZeroOne(false)) {
				//x * 1 -> x, 1 * x -> x
				setResult(otherOperand);
			} else if (operator == Operator.DIV && firstIsSimplex && simplexOperand.isZeroOne(true)) {
				//0 / x -> 0
				setResult(simplexOperand);
			} else if (operator == Operator.DIV && !firstIsSimplex && simplexOperand.isZeroOne(false)) {
				//x / 1 -> x
				setResult(otherOperand);
			} else if (operator == Operator.REM && firstIsSimplex && simplexOperand.isZeroOne(true)) {
				//0 % x -> 0
				setResult(simplexOperand);
			} else if (operator == Operator.REM && !firstIsSimplex && simplexOperand.isZeroOne(false)) {
				//x % 1 -> 0
				setResult(this.calc.pushInt(0).to(x.getType()).pop());
			} else if (operator == Operator.REM && !firstIsSimplex && ((Simplex) this.calc.push(simplexOperand).neg().pop()).isZeroOne(false)) {
				//x % (-1) -> 0
				setResult(this.calc.pushInt(0).to(x.getType()).pop());
			} else if (operator == Operator.DIV && firstOperand instanceof Expression && secondOperand instanceof Simplex) {
				//(x / n1) / n2 -> x / (n1 * n2)
				final Expression firstOperandExpression = (Expression) x.getFirstOperand();
				if (firstOperandExpression.getOperator() == Operator.DIV && firstOperandExpression.getSecondOperand() instanceof Simplex) {
					setResult(this.calc.push(firstOperandExpression.getFirstOperand()).div(this.calc.push(firstOperandExpression.getSecondOperand()).mul(secondOperand).pop()).pop());
				} else {
					setResult(x);
				}
			} else {
				setResult(x);
			}
		} catch (NoSuchElementException | NoResultException | InvalidTypeException | InvalidOperandException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }
}
