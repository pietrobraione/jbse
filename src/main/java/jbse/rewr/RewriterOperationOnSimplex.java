package jbse.rewr;

import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.ERROR;
import static jbse.common.Type.isPrimitiveFloating;
import static jbse.common.Type.isPrimitiveIntegral;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.INT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.lub;
import static jbse.common.Type.SHORT;
import static jbse.val.PrimitiveSymbolicApply.ABS_DOUBLE;
import static jbse.val.PrimitiveSymbolicApply.ABS_FLOAT;
import static jbse.val.PrimitiveSymbolicApply.ABS_INT;
import static jbse.val.PrimitiveSymbolicApply.ABS_LONG;
import static jbse.val.PrimitiveSymbolicApply.ACOS;
import static jbse.val.PrimitiveSymbolicApply.ASIN;
import static jbse.val.PrimitiveSymbolicApply.ATAN;
import static jbse.val.PrimitiveSymbolicApply.COS;
import static jbse.val.PrimitiveSymbolicApply.EXP;
import static jbse.val.PrimitiveSymbolicApply.MAX_DOUBLE;
import static jbse.val.PrimitiveSymbolicApply.MAX_FLOAT;
import static jbse.val.PrimitiveSymbolicApply.MAX_INT;
import static jbse.val.PrimitiveSymbolicApply.MAX_LONG;
import static jbse.val.PrimitiveSymbolicApply.MIN_DOUBLE;
import static jbse.val.PrimitiveSymbolicApply.MIN_FLOAT;
import static jbse.val.PrimitiveSymbolicApply.MIN_INT;
import static jbse.val.PrimitiveSymbolicApply.MIN_LONG;
import static jbse.val.PrimitiveSymbolicApply.POW;
import static jbse.val.PrimitiveSymbolicApply.SIN;
import static jbse.val.PrimitiveSymbolicApply.SQRT;
import static jbse.val.PrimitiveSymbolicApply.TAN;

import java.util.NoSuchElementException;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * Rewrites all the {@link Expression}s or {@link PrimitiveSymbolicApply}s 
 * with enough {@link Simplex} operands to calculate a result.
 * 
 * @author Pietro Braione
 */
public class RewriterOperationOnSimplex extends RewriterCalculatorRewriting {
    public RewriterOperationOnSimplex() { }

    @Override
    protected void rewritePrimitiveSymbolicApply(PrimitiveSymbolicApply x) 
    throws NoResultException {
        final int nargs = x.getArgs().length;
        final Object[] args = new Object[nargs];
        final char[] argsType = new char[nargs];
        int i = 0;
        for (Value v : x.getArgs()) {
            if (v instanceof Simplex) {
                args[i] = ((Simplex) v).getActualValue(); 
                argsType[i] = v.getType();
            } else {
                setResult(x);
                return;
            }
            i++;
        }

        final String function = x.getOperator();
        final Primitive result = tryFunctionApplication(function, args, argsType);
        if (result == null) { 
        	setResult(x); //failed
        } else {
            setResult(result);
        }
    }

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

        if (firstOperand instanceof Any || secondOperand instanceof Any) {
            //one of the operands is Any: return Any
            setResult(this.calc.valAny()); 
        } else if (firstOperand instanceof Simplex && (unary || secondOperand instanceof Simplex)) {
            //all operands are Simplex: apply operator and return Simplex
            applyOperator((Simplex) firstOperand, operator, (Simplex) secondOperand);
        } else if (operator == Operator.AND && firstOperand.equals(secondOperand)) {
            //x && x -> x
        	setResult(x.getFirstOperand());
        } else if (operator == Operator.OR && firstOperand.equals(secondOperand)) {
            //x || x -> x
        	setResult(x.getFirstOperand());
        } else if (operator == Operator.NOT && firstOperand instanceof Expression) {
        	//negation of expression
        	attemptRewriteNegationOfExpression(x);
        } else if (!unary && (firstOperand instanceof Simplex || secondOperand instanceof Simplex)) {
            //binary operation with one Simplex operand
            attemptRewriteBinaryWithSimplex(x);
        } else {
        	//none of the above cases
        	setResult(x);
        }
    }
    
    private void attemptRewriteNegationOfExpression(Expression x) throws NoResultException {
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
            } else {
            	setResult(x);
            }
            //TODO could we propagate more?
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
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

    @Override
    protected void rewriteWideningConversion(WideningConversion x) 
    throws NoResultException {
        final Primitive arg = x.getArg();

        if (x.getType() == arg.getType()) {
            //unitary widening (should never happen)
            setResult(arg);
        } else if (arg instanceof Simplex) {
            //widening applied to Simplex
            final Simplex argSimplex = (Simplex) arg;
            applyConversion(argSimplex, x.getType());
        } else {
        	//none of the above cases
        	setResult(x);
        }
    }

    @Override
    protected void rewriteNarrowingConversion(NarrowingConversion x)
    throws NoResultException {
        final Primitive arg = x.getArg();

        if (x.getType() == arg.getType()) {
            //unitary narrowing (should never happen)
            setResult(arg);
        } else if (arg instanceof Simplex) {
            //narrowing applied to Simplex
            final Simplex argSimplex = (Simplex) arg;
            applyConversion(argSimplex, x.getType());
        } else if (arg instanceof WideningConversion) {
            //narrowing a widening back to the starting type
        	final Primitive wideningArg = ((WideningConversion) arg).getArg();
        	if (x.getType() == wideningArg.getType()) {
        		setResult(wideningArg);
        	} else {
        		setResult(x);
        	}
        } else {
        	//none of the above cases
        	setResult(x);
        }
    }

    //////////////////////////////////////////////
    //private part: tons of manual dispatching
    
    private Primitive tryFunctionApplicationAbs(String function, Object arg, char argType) {
    	final Primitive retVal;
        if (function.equals(ABS_DOUBLE) && argType == DOUBLE) { //typing: D -> D
        	retVal = this.calc.val_(Math.abs(((Double) arg).doubleValue()));
        } else if (function.equals(ABS_FLOAT) && argType == FLOAT) { //typing: F -> F
        	retVal = this.calc.val_(Math.abs(((Float) arg).floatValue())); 
        } else if (function.equals(ABS_LONG) && argType == LONG) { //typing: J -> J
        	retVal = this.calc.val_(Math.abs(((Long) arg).longValue())); 
        } else if (function.equals(ABS_INT)) { //typing: T -> T
            if (argType == INT)   { 
            	retVal = this.calc.val_(Math.abs(((Integer) arg).intValue())); 
            } else if (argType == SHORT) { 
            	retVal = this.calc.val_((short) Math.abs(((Short) arg).shortValue())); 
            } else if (argType == BYTE)  { 
            	retVal = this.calc.val_((byte) Math.abs(((Byte) arg).byteValue())); 
            } else if (argType == CHAR)  { 
            	retVal = this.calc.val_((char) Math.abs(((Character) arg).charValue())); 
            } else {
            	retVal = null;
            }
        } else {
        	retVal = null;
        }
        return retVal;
    }
    
    private Primitive tryFunctionApplicationMin(String function, Object arg0, Object arg1, char argType0, char argType1) {
    	final Primitive retVal;
        if (function.equals(MIN_DOUBLE) && argType0 == DOUBLE && argType1 == DOUBLE) { //typing: D -> D
        	retVal = this.calc.val_(Math.min(((Double) arg0).doubleValue(), ((Double) arg1).doubleValue()));
        } else if (function.equals(MIN_FLOAT) && argType0 == FLOAT && argType1 == FLOAT) { //typing: F -> F
        	retVal = this.calc.val_(Math.min(((Float) arg0).floatValue(), ((Float) arg1).floatValue())); 
        } else if (function.equals(MIN_LONG) && argType0 == LONG && argType1 == LONG) { //typing: J -> J
        	retVal = this.calc.val_(Math.min(((Long) arg0).longValue(), ((Long) arg1).longValue())); 
        } else if (function.equals(MIN_INT)) { //typing: T -> T
            if (argType0 == INT && argType1 == INT)   { 
            	retVal = this.calc.val_(Math.min(((Integer) arg0).intValue(), ((Integer) arg1).intValue())); 
            } else if (argType0 == SHORT && argType1 == SHORT) { 
            	retVal = this.calc.val_((short) Math.min(((Short) arg0).shortValue(), ((Short) arg1).shortValue())); 
            } else if (argType0 == BYTE && argType1 == BYTE)  { 
            	retVal = this.calc.val_((byte) Math.min(((Byte) arg0).byteValue(), ((Byte) arg1).byteValue())); 
            } else if (argType0 == CHAR && argType1 == CHAR)  { 
            	retVal = this.calc.val_((char) Math.min(((Character) arg0).charValue(), ((Character) arg1).charValue())); 
            } else {
            	retVal = null;
            }
        } else {
        	retVal = null;
        }
        return retVal;
    }
    
    private Primitive tryFunctionApplicationMax(String function, Object arg0, Object arg1, char argType0, char argType1) {
    	final Primitive retVal;
        if (function.equals(MAX_DOUBLE) && argType0 == DOUBLE && argType1 == DOUBLE) { //typing: D -> D
        	retVal = this.calc.val_(Math.max(((Double) arg0).doubleValue(), ((Double) arg1).doubleValue()));
        } else if (function.equals(MAX_FLOAT) && argType0 == FLOAT && argType1 == FLOAT) { //typing: F -> F
        	retVal = this.calc.val_(Math.max(((Float) arg0).floatValue(), ((Float) arg1).floatValue())); 
        } else if (function.equals(MAX_LONG) && argType0 == LONG && argType1 == LONG) { //typing: J -> J
        	retVal = this.calc.val_(Math.max(((Long) arg0).longValue(), ((Long) arg1).longValue())); 
        } else if (function.equals(MAX_INT)) { //typing: T -> T
            if (argType0 == INT && argType1 == INT)   { 
            	retVal = this.calc.val_(Math.max(((Integer) arg0).intValue(), ((Integer) arg1).intValue())); 
            } else if (argType0 == SHORT && argType1 == SHORT) { 
            	retVal = this.calc.val_((short) Math.max(((Short) arg0).shortValue(), ((Short) arg1).shortValue())); 
            } else if (argType0 == BYTE && argType1 == BYTE)  { 
            	retVal = this.calc.val_((byte) Math.max(((Byte) arg0).byteValue(), ((Byte) arg1).byteValue())); 
            } else if (argType0 == CHAR && argType1 == CHAR)  { 
            	retVal = this.calc.val_((char) Math.max(((Character) arg0).charValue(), ((Character) arg1).charValue())); 
            } else {
            	retVal = null;
            }
        } else {
        	retVal = null;
        }
        return retVal;
    }
    
    private Primitive tryFunctionApplicationSin(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.sin(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.sin(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.sin(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.sin(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.sin(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.sin(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.sin(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }
    
    private Primitive tryFunctionApplicationCos(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.cos(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.cos(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.cos(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.cos(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.cos(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.cos(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.cos(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }

    private Primitive tryFunctionApplicationTan(Object arg, char argType) {
    	 //typing: T -> D
    	final Primitive retVal;
        if (argType == DOUBLE) {
        	retVal = this.calc.val_(Math.tan(((Double) arg).doubleValue())); 
        } else if (argType == FLOAT) {
        	retVal = this.calc.val_(Math.tan(((Float) arg).floatValue())); 
        } else if (argType == LONG) {
        	retVal = this.calc.val_(Math.tan(((Long) arg).longValue())); 
        } else if (argType == INT) {
        	retVal = this.calc.val_(Math.tan(((Integer) arg).intValue())); 
        } else if (argType == SHORT) {
        	retVal = this.calc.val_(Math.tan(((Short) arg).shortValue())); 
        } else if (argType == BYTE) {
        	retVal = this.calc.val_(Math.tan(((Byte) arg).byteValue())); 
        } else if (argType == CHAR) {
        	retVal = this.calc.val_(Math.tan(((Character) arg).charValue())); 
        } else {
        	retVal = null;
        }
        return retVal;
    }
    
    private Primitive tryFunctionApplicationAsin(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.asin(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.asin(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.asin(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.asin(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.asin(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.asin(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.asin(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }

    private Primitive tryFunctionApplicationAcos(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.acos(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.acos(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.acos(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.acos(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.acos(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.acos(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.acos(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }

    private Primitive tryFunctionApplicationAtan(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.atan(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.atan(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.atan(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.atan(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.atan(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.atan(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.atan(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }

    private Primitive tryFunctionApplicationSqrt(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.sqrt(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.sqrt(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.sqrt(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.sqrt(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.sqrt(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.sqrt(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.sqrt(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }

    private Primitive tryFunctionApplicationExp(Object arg, char argType) {
    	//typing: T -> D
    	final Primitive retVal;
    	if (argType == DOUBLE) {
    		retVal = this.calc.val_(Math.exp(((Double) arg).doubleValue())); 
    	} else if (argType == FLOAT) {
    		retVal = this.calc.val_(Math.exp(((Float) arg).floatValue())); 
    	} else if (argType == LONG) {
    		retVal = this.calc.val_(Math.exp(((Long) arg).longValue())); 
    	} else if (argType == INT) {
    		retVal = this.calc.val_(Math.exp(((Integer) arg).intValue())); 
    	} else if (argType == SHORT) {
    		retVal = this.calc.val_(Math.exp(((Short) arg).shortValue())); 
    	} else if (argType == BYTE) {
    		retVal = this.calc.val_(Math.exp(((Byte) arg).byteValue())); 
    	} else if (argType == CHAR) {
    		retVal = this.calc.val_(Math.exp(((Character) arg).charValue())); 
    	} else {
    		retVal = null;
    	}
    	return retVal;
    }

    private Primitive tryFunctionApplicationPow(Object arg0, Object arg1, char argType0, char argType1) {
    	final Primitive retVal;
        if (argType0 == DOUBLE && argType1 == DOUBLE) {
        	//typing: D x D -> D
        	retVal = this.calc.val_(Math.pow(((Double) arg0).doubleValue(), ((Double) arg1).doubleValue())); 
        } else if (argType0 == FLOAT && argType1 == FLOAT) {
        	//typing: F x F -> F
        	retVal = this.calc.val_((float) Math.pow(((Float) arg0).floatValue(), ((Float) arg1).floatValue())); 
        } else if (isPrimitiveIntegral(argType0) && isPrimitiveIntegral(argType1)) {
            final long exponent = ((Number) arg1).longValue();
            if (exponent >= 0) {
            	//typing: T1 x T2 -> J
                final long base = ((Number) arg0).longValue();
                long result = 1;
                for (long i = 1; i <= exponent; ++i) {
                    result *= base;
                }
                retVal = this.calc.val_(result);
            } else {
            	//typing: T1 x T2 -> D
            	retVal = this.calc.val_(Math.pow(((Number) arg0).doubleValue(), ((Number) arg1).doubleValue()));
            }
        } else {
        	retVal = null;
        }
        return retVal;
    }
    
    private Primitive tryFunctionApplication(String function, Object[] args, char[] argsType) {
    	final Primitive retVal;
    	
        if (function.equals(ABS_DOUBLE) || function.equals(ABS_FLOAT) || function.equals(ABS_LONG) || function.equals(ABS_INT)) {
        	retVal = tryFunctionApplicationAbs(function, args[0], argsType[0]);
        } else if (function.equals(MIN_DOUBLE) || function.equals(MIN_FLOAT) || function.equals(MIN_LONG) || function.equals(MIN_INT)) {
        	retVal = tryFunctionApplicationMin(function, args[0], args[1], argsType[0], argsType[1]);
        } else if (function.equals(MAX_DOUBLE) || function.equals(MAX_FLOAT) || function.equals(MAX_LONG) || function.equals(MAX_INT)) {
        	retVal = tryFunctionApplicationMax(function, args[0], args[1], argsType[0], argsType[1]);
        } else if (function.equals(SIN)) { 
        	retVal = tryFunctionApplicationSin(args[0], argsType[0]);
        } else if (function.equals(COS)) { 
        	retVal = tryFunctionApplicationCos(args[0], argsType[0]);
        } else if (function.equals(TAN)) { 
        	retVal = tryFunctionApplicationTan(args[0], argsType[0]);
        } else if (function.equals(ASIN)) { 
        	retVal = tryFunctionApplicationAsin(args[0], argsType[0]);
        } else if (function.equals(ACOS)) { 
        	retVal = tryFunctionApplicationAcos(args[0], argsType[0]);
        } else if (function.equals(ATAN)) { 
        	retVal = tryFunctionApplicationAtan(args[0], argsType[0]);
        } else if (function.equals(SQRT)) { 
        	retVal = tryFunctionApplicationSqrt(args[0], argsType[0]);
        } else if (function.equals(EXP)) { 
        	retVal = tryFunctionApplicationExp(args[0], argsType[0]);
        } else if (function.equals(POW)) { 
        	retVal = tryFunctionApplicationPow(args[0], args[1], argsType[0], argsType[1]);
        } else {
        	retVal = null;
        }
        
        return retVal;
    }

    private void applyOperatorBooleanBinary(Simplex firstOperand, Operator operator, Simplex secondOperand)
    throws NoResultException {
    	final char firstOperandType = firstOperand.getType();
    	final Object firstOperandValue = ((Simplex) firstOperand).getActualValue();
    	final char secondOperandType = secondOperand.getType();
    	final Object secondOperandValue = ((Simplex) secondOperand).getActualValue();

    	final boolean firstOperandBoolean;
    	if (firstOperandType == BOOLEAN) {
    		firstOperandBoolean = ((Boolean) firstOperandValue).booleanValue();
    	} else if (firstOperandType == BYTE) {
    		firstOperandBoolean = ((Byte) firstOperandValue).byteValue() != 0;
    	} else if (firstOperandType == CHAR) {
    		firstOperandBoolean = ((Character) firstOperandValue).charValue() != 0;
    	} else if (firstOperandType == INT) {
    		firstOperandBoolean = ((Integer) firstOperandValue).intValue() != 0;
    	} else if (firstOperandType == LONG) {
    		firstOperandBoolean = ((Long) firstOperandValue).longValue() != 0;
    	} else if (firstOperandType == SHORT) {
    		firstOperandBoolean = ((Short) firstOperandValue).shortValue() != 0;
    	} else {
    		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
    	}
    	final boolean secondOperandBoolean;
    	if (secondOperandType == BOOLEAN) {
    		secondOperandBoolean = ((Boolean) secondOperandValue).booleanValue();
    	} else if (secondOperandType == BYTE) {
    		secondOperandBoolean = ((Byte) secondOperandValue).byteValue() != 0;
    	} else if (secondOperandType == CHAR) {
    		secondOperandBoolean = ((Character) secondOperandValue).charValue() != 0;
    	} else if (secondOperandType == INT) {
    		secondOperandBoolean = ((Integer) secondOperandValue).intValue() != 0;
    	} else if (secondOperandType == LONG) {
    		secondOperandBoolean = ((Long) secondOperandValue).longValue() != 0;
    	} else if (secondOperandType == SHORT) {
    		secondOperandBoolean = ((Short) secondOperandValue).shortValue() != 0;
    	} else {
    		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
    	}

    	if (operator == Operator.AND) {
    		setResult(this.calc.val_(firstOperandBoolean && secondOperandBoolean));
    	} else if (operator == Operator.OR) {
    		setResult(this.calc.val_(firstOperandBoolean || secondOperandBoolean));
    	} else {
    		throw new UnexpectedInternalException("Found unexpected operator " + operator);
    	}
    }

    private void applyOperator(Simplex firstOperand, Operator operator, Simplex secondOperand)
    throws NoResultException {
        final char firstOperandType = firstOperand.getType();
        final Object firstOperandValue = ((Simplex) firstOperand).getActualValue();
        final char secondOperandType = (secondOperand == null ? ERROR : secondOperand.getType());
        final Object secondOperandValue = (secondOperand == null ? null : ((Simplex) secondOperand).getActualValue());

        if (operator == Operator.NOT) {
        	if (firstOperandType == BOOLEAN) {
            	setResult(this.calc.val_(!((Boolean) firstOperandValue).booleanValue()));
        	} else if (firstOperandType == BYTE) {
        		setResult(this.calc.val_(!(((Byte) firstOperandValue).byteValue() != 0)));
        	} else if (firstOperandType == CHAR) {
        		setResult(this.calc.val_(!(((Character) firstOperandValue).charValue() != 0)));
        	} else if (firstOperandType == INT) {
        		setResult(this.calc.val_(!(((Integer) firstOperandValue).intValue() != 0)));
        	} else if (firstOperandType == LONG) {
        		setResult(this.calc.val_(!(((Long) firstOperandValue).longValue() != 0)));
        	} else if (firstOperandType == SHORT) {
        		setResult(this.calc.val_(!(((Short) firstOperandValue).shortValue() != 0)));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.NEG) {
        	if (firstOperandType == DOUBLE) {
        		setResult(this.calc.val_(-((Double) firstOperandValue).doubleValue()));
        	} else if (firstOperandType == FLOAT) {
        		setResult(this.calc.val_(-((Float) firstOperandValue).floatValue()));
        	} else if (firstOperandType == LONG) {
        		setResult(this.calc.val_(-((Long) firstOperandValue).longValue()));
        	} else if (firstOperandType == INT) {
        		setResult(this.calc.val_(-((Integer) firstOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.ADD) {
        	if (firstOperandType == DOUBLE && secondOperandType == DOUBLE) {
        		setResult(this.calc.val_(((Double) firstOperandValue).doubleValue() + ((Double) secondOperandValue).doubleValue()));
        	} else if (firstOperandType == FLOAT && secondOperandType == FLOAT) {
        		setResult(this.calc.val_(((Float) firstOperandValue).floatValue() + ((Float) secondOperandValue).floatValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() + ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() + ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.SUB) {
        	if (firstOperandType == DOUBLE && secondOperandType == DOUBLE) {
        		setResult(this.calc.val_(((Double) firstOperandValue).doubleValue() - ((Double) secondOperandValue).doubleValue()));
        	} else if (firstOperandType == FLOAT && secondOperandType == FLOAT) {
        		setResult(this.calc.val_(((Float) firstOperandValue).floatValue() - ((Float) secondOperandValue).floatValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() - ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() - ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.MUL) {
        	if (firstOperandType == DOUBLE && secondOperandType == DOUBLE) {
        		setResult(this.calc.val_(((Double) firstOperandValue).doubleValue() * ((Double) secondOperandValue).doubleValue()));
        	} else if (firstOperandType == FLOAT && secondOperandType == FLOAT) {
        		setResult(this.calc.val_(((Float) firstOperandValue).floatValue() * ((Float) secondOperandValue).floatValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() * ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() * ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.DIV) {
        	if (firstOperandType == DOUBLE && secondOperandType == DOUBLE) {
        		setResult(this.calc.val_(((Double) firstOperandValue).doubleValue() / ((Double) secondOperandValue).doubleValue()));
        	} else if (firstOperandType == FLOAT && secondOperandType == FLOAT) {
        		setResult(this.calc.val_(((Float) firstOperandValue).floatValue() / ((Float) secondOperandValue).floatValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() / ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() / ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.REM) {
        	if (firstOperandType == DOUBLE && secondOperandType == DOUBLE) {
        		setResult(this.calc.val_(((Double) firstOperandValue).doubleValue() % ((Double) secondOperandValue).doubleValue()));
        	} else if (firstOperandType == FLOAT && secondOperandType == FLOAT) {
        		setResult(this.calc.val_(((Float) firstOperandValue).floatValue() % ((Float) secondOperandValue).floatValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() % ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() % ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.SHL) {
        	if (firstOperandType == INT && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() << ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT){
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() << ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.SHR) {
        	if (firstOperandType == INT && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() >> ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT){
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() >> ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.USHR) {
        	if (firstOperandType == INT && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() >>> ((Long) secondOperandValue).longValue()));
        	} else if (firstOperandType == INT && secondOperandType == INT){
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() >>> ((Integer) secondOperandValue).intValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.ANDBW) {
        	if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() & ((Integer) secondOperandValue).intValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() & ((Long) secondOperandValue).longValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.ORBW) {
        	if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() | ((Integer) secondOperandValue).intValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() | ((Long) secondOperandValue).longValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.XORBW) {
        	if (firstOperandType == INT && secondOperandType == INT) {
        		setResult(this.calc.val_(((Integer) firstOperandValue).intValue() ^ ((Integer) secondOperandValue).intValue()));
        	} else if (firstOperandType == LONG && secondOperandType == LONG) {
        		setResult(this.calc.val_(((Long) firstOperandValue).longValue() ^ ((Long) secondOperandValue).longValue()));
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
        	}
        } else if (operator == Operator.AND || operator == Operator.OR) {
        	applyOperatorBooleanBinary(firstOperand, operator, secondOperand);
        } else if (operator == Operator.EQ) {
        	final char operandsTypesLub = lub(firstOperandType, secondOperandType);
        	if (isPrimitiveFloating(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() == ((Number) secondOperandValue).doubleValue()))); 
        	} else if (isPrimitiveIntegral(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() == ((Number) secondOperandValue).longValue()))); 
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed comparison expression");
        	}
        } else if (operator == Operator.NE) {
        	final char operandsTypesLub = lub(firstOperandType, secondOperandType);
        	if (isPrimitiveFloating(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() != ((Number) secondOperandValue).doubleValue()))); 
        	} else if (isPrimitiveIntegral(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() != ((Number) secondOperandValue).longValue()))); 
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed comparison expression");
        	}
        } else if (operator == Operator.GT) {
        	final char operandsTypesLub = lub(firstOperandType, secondOperandType);
        	if (isPrimitiveFloating(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() > ((Number) secondOperandValue).doubleValue()))); 
        	} else if (isPrimitiveIntegral(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() > ((Number) secondOperandValue).longValue()))); 
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed comparison expression");
        	}
        } else if (operator == Operator.GE) {
        	final char operandsTypesLub = lub(firstOperandType, secondOperandType);
        	if (isPrimitiveFloating(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() >= ((Number) secondOperandValue).doubleValue()))); 
        	} else if (isPrimitiveIntegral(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() >= ((Number) secondOperandValue).longValue()))); 
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed comparison expression");
        	}
        } else if (operator == Operator.LT) {
        	final char operandsTypesLub = lub(firstOperandType, secondOperandType);
        	if (isPrimitiveFloating(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() < ((Number) secondOperandValue).doubleValue()))); 
        	} else if (isPrimitiveIntegral(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() < ((Number) secondOperandValue).longValue()))); 
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed comparison expression");
        	}
        } else if (operator == Operator.LE) {
        	final char operandsTypesLub = lub(firstOperandType, secondOperandType);
        	if (isPrimitiveFloating(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() <= ((Number) secondOperandValue).doubleValue()))); 
        	} else if (isPrimitiveIntegral(operandsTypesLub)) { 
        		setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() <= ((Number) secondOperandValue).longValue()))); 
        	} else {
        		throw new UnexpectedInternalException("Found ill-formed comparison expression");
        	}
        } else {
        	throw new UnexpectedInternalException("Found unexpected operator " + operator);
        }
    } 

    private void applyConversion(Simplex toConvert, char to) 
    throws NoResultException {
        final char from = toConvert.getType();
        final Number n;
        if (from == CHAR) {
            final char c = ((Character) toConvert.getActualValue()).charValue();
            n = Integer.valueOf((int) c);
        } else if (from == BOOLEAN) {
            final boolean b = ((Boolean) toConvert.getActualValue()).booleanValue();
            n = Integer.valueOf(b ? 1 : 0);
        } else {
            n = (Number) toConvert.getActualValue();
        }
        if (to == BOOLEAN) {
            setResult(this.calc.val_((n.intValue() == 0 ? false : true)));
        } else if (to == BYTE) {
            setResult(this.calc.val_(n.byteValue()));
        } else if (to == SHORT) {
            setResult(this.calc.val_(n.shortValue()));
        } else if (to == INT) {
            setResult(this.calc.val_(n.intValue()));
        } else if (to == LONG) {
            setResult(this.calc.val_(n.longValue()));
        } else if (to == CHAR) {
            setResult(this.calc.val_((char) n.intValue()));
        } else if (to == FLOAT) {
            setResult(this.calc.val_(n.floatValue()));
        } else if (to == DOUBLE) {
            setResult(this.calc.val_(n.doubleValue()));
        } else {
            throw new NoResultException();
        }
    }
}
