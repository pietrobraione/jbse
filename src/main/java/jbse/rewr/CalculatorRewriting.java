package jbse.rewr;

import java.util.ArrayList;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.exc.NoResultException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.HistoryPoint;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

/**
 * A {@link Calculator} based on {@link Rewriter}s.
 * 
 * @author Pietro Braione
 *
 */
public class CalculatorRewriting extends Calculator {
    private final ArrayList<Rewriter> rewriters = new ArrayList<Rewriter>();

    /**
     * Constructor.
     */
    public CalculatorRewriting() {
        super();
    }
    
    /**
     * Adds a rewriter.
     * 
     * @param r the {@link Rewriter} to add.
     */
    public void addRewriter(Rewriter r) {
        this.rewriters.add(r);
    }

    @Override
    public Primitive add(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.ADD, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive mul(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.MUL, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive sub(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.SUB, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive div(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.DIV, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive rem(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.REM, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive neg(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionUnary(this, Operator.NEG, operand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive andBitwise(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.ANDBW, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive orBitwise(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.ORBW, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive xorBitwise(Primitive first, Primitive param) 
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, first, Operator.XORBW, param));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive and(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.AND, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive or(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.OR, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive not(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionUnary(this, Operator.NOT, operand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive shl(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.SHL, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive shr(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.SHR, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive ushr(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.USHR, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive eq(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.EQ, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive ne(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.NE, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive le(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.LE, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive lt(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.LT, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive ge(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.GE, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive gt(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException {
        try {
            return simplify(Expression.makeExpressionBinary(this, firstOperand, Operator.GT, secondOperand));
        } catch (InvalidOperatorException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Primitive applyFunctionPrimitive(char type, HistoryPoint historyPoint, String operator, Value... args) 
    throws InvalidOperandException, InvalidTypeException, InvalidInputException {
        return simplify(new PrimitiveSymbolicApply(type, historyPoint, this, operator, args));
    }

    @Override
    public Primitive widen(char type, Primitive arg) 
    throws InvalidTypeException, InvalidOperandException {
        try {
			return simplify(WideningConversion.make(type, this, arg));
		} catch (InvalidInputException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    @Override
    public Primitive narrow(char type, Primitive arg) 
    throws InvalidTypeException, InvalidOperandException {
        try {
			return simplify(NarrowingConversion.make(type, this, arg));
		} catch (InvalidInputException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }
    
    @Override
    public Primitive simplify(Primitive p) {
    	final Rewriter[] rewritersArray = this.rewriters.toArray(new Rewriter[this.rewriters.size()]);
    	try {
    		final Primitive retVal =  Rewriter.applyRewriters(p, this, rewritersArray);
    		return retVal;
    	} catch (NoResultException e) {
    		//this should not happen
    		throw new UnexpectedInternalException(e);
    	}
    }
}
