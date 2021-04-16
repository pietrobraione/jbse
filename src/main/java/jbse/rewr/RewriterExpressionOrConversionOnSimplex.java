package jbse.rewr;

import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.isPrimitiveFloating;
import static jbse.common.Type.isPrimitiveIntegral;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.INT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.lub;
import static jbse.common.Type.SHORT;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.WideningConversion;
import jbse.val.exc.NoResultException;

/**
 * Rewrites the {@link Expression}s,  
 * {@link WideningConversion}s and {@link NarrowingConversion}s
 * with {@link Simplex} operands to their corresponding values.
 * 
 * @author Pietro Braione
 */
public final class RewriterExpressionOrConversionOnSimplex extends RewriterCalculatorRewriting {
    public RewriterExpressionOrConversionOnSimplex() { }

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
        } else {
        	//none of the above cases
        	setResult(x);
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
            applyWideningNarrowingConversion(argSimplex, x.getType());
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
            applyWideningNarrowingConversion(argSimplex, x.getType());
        } else {
        	//none of the above cases
        	setResult(x);
        }
    }

    private void applyOperatorUnary(Simplex firstOperand, Operator operator)
    throws NoResultException {
    	final char firstOperandType = firstOperand.getType();
    	final Object firstOperandValue = ((Simplex) firstOperand).getActualValue();

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
    	} else {
    		throw new UnexpectedInternalException("Found unexpected operator " + operator);
    	}
    }

    private void applyOperatorArithBinary(Simplex firstOperand, Operator operator, Simplex secondOperand)
    throws NoResultException {
    	final char firstOperandType = firstOperand.getType();
    	final Object firstOperandValue = ((Simplex) firstOperand).getActualValue();
    	final char secondOperandType = secondOperand.getType();
    	final Object secondOperandValue = ((Simplex) secondOperand).getActualValue();

    	if (operator == Operator.ADD) {
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
    	} else {
    		throw new UnexpectedInternalException("Found unexpected operator " + operator);
    	}
    }

    
    private void applyOperatorBitwise(Simplex firstOperand, Operator operator, Simplex secondOperand)
    throws NoResultException {
    	final char firstOperandType = firstOperand.getType();
    	final Object firstOperandValue = ((Simplex) firstOperand).getActualValue();
    	final char secondOperandType = secondOperand.getType();
    	final Object secondOperandValue = ((Simplex) secondOperand).getActualValue();

    	if (operator == Operator.SHL) {
    		if (firstOperandType == LONG && secondOperandType == INT) {
    			setResult(this.calc.val_(((Long) firstOperandValue).longValue() << ((Integer) secondOperandValue).intValue()));
    		} else if (firstOperandType == INT && secondOperandType == INT){
    			setResult(this.calc.val_(((Integer) firstOperandValue).intValue() << ((Integer) secondOperandValue).intValue()));
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
    		}
    	} else if (operator == Operator.SHR) {
    		if (firstOperandType == LONG && secondOperandType == INT) {
    			setResult(this.calc.val_(((Long) firstOperandValue).longValue() >> ((Integer) secondOperandValue).intValue()));
    		} else if (firstOperandType == INT && secondOperandType == INT){
    			setResult(this.calc.val_(((Integer) firstOperandValue).intValue() >> ((Integer) secondOperandValue).intValue()));
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed arithmetic expression");
    		}
    	} else if (operator == Operator.USHR) {
    		if (firstOperandType == LONG && secondOperandType == INT) {
    			setResult(this.calc.val_(((Long) firstOperandValue).longValue() >>> ((Integer) secondOperandValue).intValue()));
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
    	} else {
    		throw new UnexpectedInternalException("Found unexpected operator " + operator);
    	}
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
    
    private void applyOperatorRelational(Simplex firstOperand, Operator operator, Simplex secondOperand) 
    throws NoResultException {
    	final char firstOperandType = firstOperand.getType();
    	final Object firstOperandValue = ((Simplex) firstOperand).getActualValue();
    	final char secondOperandType = secondOperand.getType();
    	final Object secondOperandValue = ((Simplex) secondOperand).getActualValue();

		final char operandsTypesLub = lub(firstOperandType, secondOperandType);
    	if (operator == Operator.EQ) {
    		if (isPrimitiveFloating(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() == ((Number) secondOperandValue).doubleValue()))); 
    		} else if (isPrimitiveIntegral(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() == ((Number) secondOperandValue).longValue()))); 
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed comparison expression");
    		}
    	} else if (operator == Operator.NE) {
    		if (isPrimitiveFloating(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() != ((Number) secondOperandValue).doubleValue()))); 
    		} else if (isPrimitiveIntegral(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() != ((Number) secondOperandValue).longValue()))); 
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed comparison expression");
    		}
    	} else if (operator == Operator.GT) {
    		if (isPrimitiveFloating(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() > ((Number) secondOperandValue).doubleValue()))); 
    		} else if (isPrimitiveIntegral(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() > ((Number) secondOperandValue).longValue()))); 
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed comparison expression");
    		}
    	} else if (operator == Operator.GE) {
    		if (isPrimitiveFloating(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() >= ((Number) secondOperandValue).doubleValue()))); 
    		} else if (isPrimitiveIntegral(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() >= ((Number) secondOperandValue).longValue()))); 
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed comparison expression");
    		}
    	} else if (operator == Operator.LT) {
    		if (isPrimitiveFloating(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).doubleValue() < ((Number) secondOperandValue).doubleValue()))); 
    		} else if (isPrimitiveIntegral(operandsTypesLub)) { 
    			setResult(this.calc.val_((boolean) (((Number) firstOperandValue).longValue() < ((Number) secondOperandValue).longValue()))); 
    		} else {
    			throw new UnexpectedInternalException("Found ill-formed comparison expression");
    		}
    	} else if (operator == Operator.LE) {
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

    private void applyOperator(Simplex firstOperand, Operator operator, Simplex secondOperand)
    throws NoResultException {
        if (operator == Operator.NOT || operator == Operator.NEG) {
        	applyOperatorUnary(firstOperand, operator);
        } else if (operator == Operator.ADD || operator == Operator.SUB || operator == Operator.MUL || operator == Operator.DIV || operator == Operator.REM) {
        	applyOperatorArithBinary(firstOperand, operator, secondOperand);
        } else if (operator == Operator.SHL || operator == Operator.SHR || operator == Operator.USHR || operator == Operator.ANDBW || operator == Operator.ORBW || operator == Operator.XORBW) {
        	applyOperatorBitwise(firstOperand, operator, secondOperand);
        } else if (operator == Operator.AND || operator == Operator.OR) {
        	applyOperatorBooleanBinary(firstOperand, operator, secondOperand);
        } else if (operator == Operator.EQ || operator == Operator.NE || operator == Operator.GT || operator == Operator.GE || operator == Operator.LT || operator == Operator.LE) {
        	applyOperatorRelational(firstOperand, operator, secondOperand);
        } else {
        	throw new UnexpectedInternalException("Found unexpected operator " + operator);
        }
    } 

    private void applyWideningNarrowingConversion(Simplex toConvert, char to) 
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
