package jbse.rewr;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites all the {@link Expression}s by attempting negation elimination.
 * 
 * @author Pietro Braione
 */
public final class RewriterNegationElimination extends RewriterCalculatorRewriting {
    public RewriterNegationElimination() { }

    @Override
    protected void rewriteExpression(Expression x) 
    throws NoResultException {        
        final Operator operator = x.getOperator();

        if (operator == Operator.NOT && x.getOperand() instanceof Expression) {
        	//negation of expression
        	attemptRewriteNegationElimination(x);
        } else {
        	//none of the above cases
        	setResult(x);
        }
    }

    private void attemptRewriteNegationElimination(Expression x) throws NoResultException {
    	final Primitive operand = x.getOperand();
        final Expression operandExpression = (Expression) operand;
        final Operator operandOperator = operandExpression.getOperator();
        try {
            if (operandOperator == Operator.EQ) {
            	//! (x == y) -> x != y
                setResult(this.calc.push(operandExpression.getFirstOperand()).ne(operandExpression.getSecondOperand()).pop());
            } else if (operandOperator == Operator.NE) {
            	//! (x != y) -> x == y
                setResult(this.calc.push(operandExpression.getFirstOperand()).eq(operandExpression.getSecondOperand()).pop());
            } else if (operandOperator == Operator.GT) {
            	//! (x > y) -> x <= y
                setResult(this.calc.push(operandExpression.getFirstOperand()).le(operandExpression.getSecondOperand()).pop());
            } else if (operandOperator == Operator.GE) {
            	//! (x >= y) -> x < y
                setResult(this.calc.push(operandExpression.getFirstOperand()).lt(operandExpression.getSecondOperand()).pop());
            } else if (operandOperator == Operator.LT) {
            	//! (x < y) -> x >= y
                setResult(this.calc.push(operandExpression.getFirstOperand()).ge(operandExpression.getSecondOperand()).pop());
            } else if (operandOperator == Operator.LE) {
            	//! (x <= y) -> x > y
                setResult(this.calc.push(operandExpression.getFirstOperand()).gt(operandExpression.getSecondOperand()).pop());
            } else if (operandOperator == Operator.AND) {
            	//! (x && y) -> !x || !y
            	final Primitive secondOperandNegated = this.calc.push(operandExpression.getSecondOperand()).not().pop();
                setResult(this.calc.push(operandExpression.getFirstOperand()).not().or(secondOperandNegated).pop());
            } else if (operandOperator == Operator.OR) {
            	//! (x || y) -> !x && !y
            	final Primitive secondOperandNegated = this.calc.push(operandExpression.getSecondOperand()).not().pop();
                setResult(this.calc.push(operandExpression.getFirstOperand()).not().and(secondOperandNegated).pop());
            } else if (operandOperator == Operator.NOT) {
            	//! (!x) -> x
                setResult(operandExpression.getOperand());
            } else {
            	setResult(x);
            }
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }    
}
