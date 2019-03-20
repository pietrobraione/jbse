package jbse.rewr;

import static jbse.val.PrimitiveSymbolicApply.*;

import jbse.common.Type;
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
        final Primitive firstOp, secondOp;

        //gets operands
        if (unary) {
            firstOp = null;
            secondOp = x.getOperand();
        } else {
            firstOp = x.getFirstOperand();
            secondOp = x.getSecondOperand();
        }

        //1- one of the operands is Any: return Any
        if (firstOp instanceof Any || secondOp instanceof Any) {
            setResult(this.calc.valAny()); 
            return;
        }

        //2- all operands are Simplex: apply operator and return Simplex
        if ((unary || firstOp instanceof Simplex) && secondOp instanceof Simplex) {
            applyOperator((Simplex) firstOp, x.getOperator(), (Simplex) secondOp);
            return;
        }

        //TODO take out all the next normalization

        //3- one operand is an additive/multiplicative zero and the operator
        //is additive/multiplicative: rewrite to the other operand
        if (!unary && (firstOp instanceof Simplex || secondOp instanceof Simplex)) {
            final boolean firstIsSimplex = (firstOp instanceof Simplex);
            final Simplex simplexOp = (Simplex) (firstIsSimplex ? firstOp : secondOp);
            final Primitive otherOp = (firstIsSimplex ? secondOp : firstOp);

            //x && true -> x, x && false -> false, true && x -> x, false && x -> false
            if (x.getOperator() == Operator.AND) {
                if ((Boolean) simplexOp.getActualValue()) {
                    setResult(otherOp);
                    return;
                } else {
                    setResult(simplexOp);
                    return;
                }
            } 

            //x || true -> true, x || false -> x, true || x -> true, false || x -> x
            if (x.getOperator() == Operator.OR) {
                if ((Boolean) simplexOp.getActualValue()) {
                    setResult(simplexOp);
                    return;
                } else {
                    setResult(otherOp);
                    return;
                }
            } 

            //x + 0 -> x, 0 + x -> x
            if (x.getOperator() == Operator.ADD && simplexOp.isZeroOne(true)) {
                setResult(otherOp);
                return;
            }

            //x - 0 -> x, 0 - x -> -x
            if (x.getOperator() == Operator.SUB && simplexOp.isZeroOne(true)) {
                if (firstIsSimplex) {
                    try {
                        setResult(this.calc.push(otherOp).neg().pop());
                        return;
                    } catch (InvalidTypeException e) {
                        //does nothing, falls through
                    } catch (InvalidOperandException e) {
                    	//this should never happen
                    	throw new UnexpectedInternalException(e);
                    }
                } else {
                    setResult(otherOp);
                    return;
                }
            }

            //x * 0 -> 0, x * 1 -> x, 0 * x -> 0, 1 * x -> x
            if (x.getOperator() == Operator.MUL) {
                if (simplexOp.isZeroOne(true)) {
                    setResult(simplexOp);
                    return;
                } 
                if (simplexOp.isZeroOne(false)) {
                    setResult(otherOp);
                    return;
                }
            } 

            //0 / x -> 0, x / 1 -> x
            if (x.getOperator() == Operator.DIV) {
                if (firstIsSimplex && simplexOp.isZeroOne(true)) {
                    setResult(simplexOp);
                    return;
                }
                if (!firstIsSimplex && simplexOp.isZeroOne(false)) {
                    setResult(otherOp);
                    return;
                }
            }

            //0 % x -> 0, x % 1 -> 0, x % (-1) -> 0
            if (x.getOperator() == Operator.REM) {
                if (firstIsSimplex && simplexOp.isZeroOne(true)) {
                    setResult(simplexOp);
                    return;
                }
                try {
                	if (!firstIsSimplex && simplexOp.isZeroOne(false)) {
                		setResult(this.calc.pushInt(0).to(x.getType()).pop());
                		return;
                	}
                	if (!firstIsSimplex && ((Simplex) this.calc.push(simplexOp).neg().pop()).isZeroOne(false)) {
                		setResult(this.calc.pushInt(0).to(x.getType()).pop());
                		return;
                	}
				} catch (InvalidOperandException | InvalidTypeException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
            }
        }

        //TODO should we rewrite the result before setting it with setResult????

        //4- the expression is the application of an operator to operand(s) for which
        //the operator is idempotent: return the operand

        //x && x -> x
        if (x.getOperator() == Operator.AND && x.getFirstOperand().equals(x.getSecondOperand())) {
            setResult(x.getFirstOperand());
            return;
        }

        //x || x -> x
        if (x.getOperator() == Operator.OR && x.getFirstOperand().equals(x.getSecondOperand())) {
            setResult(x.getFirstOperand());
            return;
        }

        //4- the expression is a negation: eliminate it and propagate innerwards
        if (unary && x.getOperator() == Operator.NOT && secondOp instanceof Expression) {
            final Expression secondExp = (Expression) secondOp;
            final Operator secondOperator = secondExp.getOperator();
            try {
                if (secondOperator == Operator.EQ) {
                    setResult(this.calc.push(secondExp.getFirstOperand()).ne(secondExp.getSecondOperand()).pop());
                    return;
                } else if (secondOperator == Operator.NE) {
                    setResult(this.calc.push(secondExp.getFirstOperand()).eq(secondExp.getSecondOperand()).pop());
                    return;
                } else if (secondOperator == Operator.GT) {
                    setResult(this.calc.push(secondExp.getFirstOperand()).le(secondExp.getSecondOperand()).pop());
                    return;
                } else if (secondOperator == Operator.GE) {
                    setResult(this.calc.push(secondExp.getFirstOperand()).lt(secondExp.getSecondOperand()).pop());
                    return;
                } else if (secondOperator == Operator.LT) {
                    setResult(this.calc.push(secondExp.getFirstOperand()).ge(secondExp.getSecondOperand()).pop());
                    return;
                } else if (secondOperator == Operator.LE) {
                    setResult(this.calc.push(secondExp.getFirstOperand()).gt(secondExp.getSecondOperand()).pop());
                    return;
                }
                //TODO could we propagate more?
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }
        
        //5- (x / n1) / n2 -> x / (n1 * n2)
        if (x.getOperator() == Operator.DIV && secondOp instanceof Simplex) {
        	if (firstOp instanceof Expression) {
        		final Expression firstOpExpression = (Expression) x.getFirstOperand();
                if (firstOpExpression.getOperator() == Operator.DIV && firstOpExpression.getSecondOperand() instanceof Simplex) {
                	try {
						setResult(this.calc.push(firstOpExpression.getFirstOperand()).div(this.calc.push(firstOpExpression.getSecondOperand()).mul(secondOp).pop()).pop());
						return;
					} catch (InvalidOperandException | InvalidTypeException e) {
		                //this should never happen
		                throw new UnexpectedInternalException(e);
					}
                }
        	}
        }

        //6- none of the above cases
        setResult(x);
    }

    @Override
    protected void rewriteWideningConversion(WideningConversion x) 
    throws NoResultException {
        final Primitive arg = x.getArg();

        //1- unitary widening (should never happen)
        if (x.getType() == arg.getType()) {
            setResult(arg);
            return;
        }

        //2- widening applied to Simplex
        if (arg instanceof Simplex) {
            final Simplex argSimplex = (Simplex) arg;
            applyConversion(argSimplex, x.getType());
            return;
        }

        //3- none of the above cases
        setResult(x);
    }

    @Override
    protected void rewriteNarrowingConversion(NarrowingConversion x)
    throws NoResultException {
        final Primitive arg = x.getArg();

        //1- unitary narrowing
        if (x.getType() == arg.getType()) {
            setResult(arg);
            return;
        }

        //2- narrowing applied to Simplex
        if (arg instanceof Simplex) {
            final Simplex argSimplex = (Simplex) arg;
            applyConversion(argSimplex, x.getType());
            return;
        }
        
        //3- narrowing a widening back to the starting type
        if (arg instanceof WideningConversion) {
        	final Primitive wideningArg = ((WideningConversion) arg).getArg();
        	if (x.getType() == wideningArg.getType()) {
        		setResult(wideningArg);
        		return;
        	}
        }

        //4- none of the above cases
        setResult(x);
    }

    //////////////////////////////////////////////
    //private part: tons of manual dispatching
    //TODO simplify this huge mess
    private Primitive tryFunctionApplication(String function, Object[] args, char[] argsType) {
        if (function.equals(ABS_DOUBLE)) { //typing: D -> D
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.abs(((Double)args[0]).doubleValue())));}
        }
        if (function.equals(ABS_FLOAT)) { //typing: F -> F
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.abs(((Float)args[0]).floatValue()))); }
        }
        if (function.equals(ABS_LONG)) { //typing: J -> J
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.abs(((Long)args[0]).longValue()))); }
        }
        if (function.equals(ABS_INT)) { //typing: T -> T
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.abs(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_((short) Math.abs(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_((byte) Math.abs(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_((char) Math.abs(((Character)args[0]).charValue()))); }
        }

        if (function.equals(MIN_DOUBLE)) { //typing: D x D -> D
            if(argsType[0] == Type.DOUBLE)    { return(this.calc.val_(Math.min(((Double)args[0]).doubleValue(), ((Double)args[1]).doubleValue()))); }
        }
        if (function.equals(MIN_FLOAT)) { //typing: F x F -> F
            if(argsType[0]== Type.FLOAT)      { return(this.calc.val_(Math.min(((Float)args[0]).floatValue(), ((Float)args[1]).floatValue()))); }
        }
        if (function.equals(MIN_LONG)) { //typing: J x J -> J
            if(argsType[0]==Type.LONG)        { return(this.calc.val_(Math.min(((Long)args[0]).longValue(), ((Long)args[1]).longValue()))); }
        }
        if (function.equals(MIN_INT)) { //typing: T x T -> T
            if(argsType[0]==Type.INT)         { return(this.calc.val_(Math.min(((Integer)args[0]).intValue(), ((Integer)args[1]).intValue()))); }
            if(argsType[0]==Type.SHORT)       { return(this.calc.val_((short) Math.min(((Short)args[0]).shortValue(), ((Short)args[1]).shortValue()))); }
            if(argsType[0]==Type.BYTE)        { return(this.calc.val_((byte)  Math.min(((Byte)args[0]).byteValue(), ((Byte)args[1]).byteValue()))); }
            if(argsType[0]==Type.CHAR)        { return(this.calc.val_((char)  Math.min(((Character)args[0]).charValue(), ((Character)args[1]).charValue()))); }
        }

        if (function.equals(MAX_DOUBLE)) { //typing: D x D -> D
            if(argsType[0] == Type.DOUBLE)    { return(this.calc.val_(Math.max(((Double)args[0]).doubleValue(), ((Double)args[1]).doubleValue()))); }
        }
        if (function.equals(MAX_FLOAT)) { //typing: F x F -> F
            if(argsType[0]== Type.FLOAT)      { return(this.calc.val_(Math.max(((Float)args[0]).floatValue(), ((Float)args[1]).floatValue()))); }
        }
        if (function.equals(MAX_LONG)) { //typing: J x J -> J
            if(argsType[0]==Type.LONG)        { return(this.calc.val_(Math.max(((Long)args[0]).longValue(), ((Long)args[1]).longValue()))); }
        }
        if (function.equals(MAX_INT)) { //typing: T x T -> T
            if(argsType[0]==Type.INT)         { return(this.calc.val_(Math.max(((Integer)args[0]).intValue(), ((Integer)args[1]).intValue()))); }
            if(argsType[0]==Type.SHORT)       { return(this.calc.val_((short) Math.max(((Short)args[0]).shortValue(), ((Short)args[1]).shortValue()))); }
            if(argsType[0]==Type.BYTE)        { return(this.calc.val_((byte)  Math.max(((Byte)args[0]).byteValue(), ((Byte)args[1]).byteValue()))); }
            if(argsType[0]==Type.CHAR)        { return(this.calc.val_((char)  Math.max(((Character)args[0]).charValue(), ((Character)args[1]).charValue()))); }
        }

        if (function.equals(SIN)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.sin(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.sin(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.sin(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.sin(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.sin(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.sin(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.sin(((Character)args[0]).charValue()))); }
        }

        if (function.equals(COS)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.cos(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.cos(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.cos(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.cos(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.cos(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.cos(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.cos(((Character)args[0]).charValue()))); }
        }

        if (function.equals(TAN)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.tan(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.tan(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.tan(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.tan(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.tan(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.tan(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.tan(((Character)args[0]).charValue()))); }
        }

        if (function.equals(ASIN)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.asin(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.asin(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.asin(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.asin(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.asin(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.asin(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.asin(((Character)args[0]).charValue()))); }
        }

        if (function.equals(ACOS)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.acos(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.acos(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.acos(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.acos(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.acos(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.acos(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.acos(((Character)args[0]).charValue()))); }
        }

        if (function.equals(ATAN)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.atan(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.atan(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.atan(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.atan(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.atan(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.atan(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.atan(((Character)args[0]).charValue()))); }
        }

        if (function.equals(SQRT)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE) {return(this.calc.val_(Math.sqrt(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT)  {return(this.calc.val_(Math.sqrt(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)   {return(this.calc.val_(Math.sqrt(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)    {return(this.calc.val_(Math.sqrt(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT)  {return(this.calc.val_(Math.sqrt(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)   {return(this.calc.val_(Math.sqrt(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)   {return(this.calc.val_(Math.sqrt(((Character)args[0]).charValue()))); }
        }

        if (function.equals(POW)) { //typing: T x T -> double, except T x T -> long when T integral and second parameter nonnegative
            if(argsType[0] == Type.DOUBLE && argsType[1] == Type.DOUBLE) {return(this.calc.val_(Math.pow(((Double)args[0]).doubleValue(), ((Double)args[1]).doubleValue()))); }
            if(argsType[0]== Type.FLOAT && argsType[1] == Type.FLOAT)    {return(this.calc.val_((float) Math.pow(((Float)args[0]).floatValue(), ((Float)args[1]).floatValue()))); }
            if(Type.isPrimitiveIntegral(argsType[0]) && Type.isPrimitiveIntegral(argsType[1])) {
                final long exponent = ((Number) args[1]).longValue();
                if (exponent >= 0) {
                    final long base = ((Number) args[0]).longValue();
                    long result = 1;
                    for (long i = 1; i <= exponent; ++i) {
                        result *= base;
                    }
                    return this.calc.val_(result);
                } else {
                    return(this.calc.val_(Math.pow(((Number)args[0]).doubleValue(), ((Number)args[1]).doubleValue())));
                }
            }
        }

        if (function.equals(EXP)) { //typing: T -> double
            if(argsType[0] == Type.DOUBLE){return(this.calc.val_(Math.exp(((Double)args[0]).doubleValue()))); }
            if(argsType[0] == Type.FLOAT) {return(this.calc.val_(Math.exp(((Float)args[0]).floatValue()))); }
            if(argsType[0] == Type.LONG)  {return(this.calc.val_(Math.exp(((Long)args[0]).longValue()))); }
            if(argsType[0] == Type.INT)   {return(this.calc.val_(Math.exp(((Integer)args[0]).intValue()))); }
            if(argsType[0] == Type.SHORT) {return(this.calc.val_(Math.exp(((Short)args[0]).shortValue()))); }
            if(argsType[0] == Type.BYTE)  {return(this.calc.val_(Math.exp(((Byte)args[0]).byteValue()))); }
            if(argsType[0] == Type.CHAR)  {return(this.calc.val_(Math.exp(((Character)args[0]).charValue()))); }
        }

        //no application
        return null;
    }

    private void applyOperator(Simplex firstOp, Operator operation, Simplex secondOp)
    throws NoResultException {
        boolean unary = (firstOp == null);
        int sOpType = secondOp.getType();
        Object sOp = ((Simplex) secondOp).getActualValue();
        if (unary) {
            if (operation == Operator.NOT) {
                setResult(this.calc.val_(!((Boolean)sOp).booleanValue()));
            } else if (operation == Operator.NEG) {
                if(sOpType==Type.DOUBLE){setResult(this.calc.val_(-((Double)sOp).doubleValue()));}
                if(sOpType==Type.FLOAT){setResult(this.calc.val_(-((Float)sOp).floatValue()));}
                if(sOpType==Type.LONG){setResult(this.calc.val_(-((Long)sOp).longValue()));}
                if(sOpType==Type.INT){setResult(this.calc.val_(-((Integer)sOp).intValue()));}
                if(sOpType==Type.SHORT){setResult(this.calc.val_(-((Short)sOp).shortValue()));}
                if(sOpType==Type.BYTE){setResult(this.calc.val_(-((Byte)sOp).byteValue()));}
                if(sOpType==Type.CHAR){setResult(this.calc.val_(-((Character)sOp).charValue()));}
            }
        } else {
            int fOpType=firstOp.getType();
            Object fOp=((Simplex)firstOp).getActualValue();
            if (operation == Operator.ADD){
                if(fOpType == Type.DOUBLE){
                    if(sOpType == Type.DOUBLE){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Double)fOp).doubleValue()+((Character)sOp).charValue()));}
                } else if(fOpType==Type.FLOAT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Float)fOp).floatValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Float)fOp).floatValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Float)fOp).floatValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Float)fOp).floatValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Float)fOp).floatValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Float)fOp).floatValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Float)fOp).floatValue()+((Character)sOp).charValue()));}
                } else if(fOpType==Type.LONG) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Long)fOp).longValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Long)fOp).longValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Long)fOp).longValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Long)fOp).longValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Long)fOp).longValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Long)fOp).longValue()+((Character)sOp).charValue()));}
                } else if(fOpType==Type.INT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Integer)fOp).intValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Integer)fOp).intValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Integer)fOp).intValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Integer)fOp).intValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Integer)fOp).intValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Integer)fOp).intValue()+((Character)sOp).charValue()));}
                } else if(fOpType==Type.SHORT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Short)fOp).shortValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Short)fOp).shortValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Short)fOp).shortValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Short)fOp).shortValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Short)fOp).shortValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Short)fOp).shortValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Short)fOp).shortValue()+((Character)sOp).charValue()));}
                } else if(fOpType==Type.BYTE) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Byte)fOp).byteValue()+((Character)sOp).charValue()));}
                } else if(fOpType==Type.CHAR) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Character)fOp).charValue()+((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Character)fOp).charValue()+((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Character)fOp).charValue()+((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Character)fOp).charValue()+((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Character)fOp).charValue()+((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Character)fOp).charValue()+((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Character)fOp).charValue()+((Character)sOp).charValue()));}
                }
            } else if(operation==Operator.MUL){
                if(fOpType==Type.DOUBLE){
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Double)fOp).doubleValue()*((Character)sOp).charValue()));}
                } else if(fOpType==Type.FLOAT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Float)fOp).floatValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Float)fOp).floatValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Float)fOp).floatValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Float)fOp).floatValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Float)fOp).floatValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Float)fOp).floatValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Float)fOp).floatValue()*((Character)sOp).charValue()));}
                } else if(fOpType==Type.LONG) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Long)fOp).longValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Long)fOp).longValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Long)fOp).longValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Long)fOp).longValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Long)fOp).longValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Long)fOp).longValue()*((Character)sOp).charValue()));}
                } else if(fOpType==Type.INT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Integer)fOp).intValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Integer)fOp).intValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Integer)fOp).intValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Integer)fOp).intValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Integer)fOp).intValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Integer)fOp).intValue()*((Character)sOp).charValue()));}
                } else if(fOpType==Type.SHORT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Short)fOp).shortValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Short)fOp).shortValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Short)fOp).shortValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Short)fOp).shortValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Short)fOp).shortValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Short)fOp).shortValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Short)fOp).shortValue()*((Character)sOp).charValue()));}
                } else if(fOpType==Type.BYTE) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Byte)fOp).byteValue()*((Character)sOp).charValue()));}
                } else if(fOpType==Type.CHAR) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Character)fOp).charValue()*((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Character)fOp).charValue()*((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Character)fOp).charValue()*((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Character)fOp).charValue()*((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Character)fOp).charValue()*((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Character)fOp).charValue()*((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Character)fOp).charValue()*((Character)sOp).charValue()));}
                }
            } else if(operation==Operator.SUB) {
                if(fOpType==Type.DOUBLE){
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Double)fOp).doubleValue()-((Character)sOp).charValue()));}
                } else if(fOpType==Type.FLOAT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Float)fOp).floatValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Float)fOp).floatValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Float)fOp).floatValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Float)fOp).floatValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Float)fOp).floatValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Float)fOp).floatValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Float)fOp).floatValue()-((Character)sOp).charValue()));}
                } else if(fOpType==Type.LONG) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Long)fOp).longValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Long)fOp).longValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Long)fOp).longValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Long)fOp).longValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Long)fOp).longValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Long)fOp).longValue()-((Character)sOp).charValue()));}
                } else if(fOpType==Type.INT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Integer)fOp).intValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Integer)fOp).intValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Integer)fOp).intValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Integer)fOp).intValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Integer)fOp).intValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Integer)fOp).intValue()-((Character)sOp).charValue()));}
                } else if(fOpType==Type.SHORT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Short)fOp).shortValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Short)fOp).shortValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Short)fOp).shortValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Short)fOp).shortValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Short)fOp).shortValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Short)fOp).shortValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Short)fOp).shortValue()-((Character)sOp).charValue()));}
                } else if(fOpType==Type.BYTE) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Byte)fOp).byteValue()-((Character)sOp).charValue()));}
                } else if(fOpType==Type.CHAR) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Character)fOp).charValue()-((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Character)fOp).charValue()-((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Character)fOp).charValue()-((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Character)fOp).charValue()-((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Character)fOp).charValue()-((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Character)fOp).charValue()-((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Character)fOp).charValue()-((Character)sOp).charValue()));}
                }
            } else if(operation==Operator.DIV) {
                if(fOpType==Type.DOUBLE) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Double)fOp).doubleValue()/((Character)sOp).charValue()));}
                } else if(fOpType==Type.FLOAT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Float)fOp).floatValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Float)fOp).floatValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Float)fOp).floatValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Float)fOp).floatValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Float)fOp).floatValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Float)fOp).floatValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Float)fOp).floatValue()/((Character)sOp).charValue()));}
                } else if(fOpType==Type.LONG) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Long)fOp).longValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Long)fOp).longValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Long)fOp).longValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Long)fOp).longValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Long)fOp).longValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Long)fOp).longValue()/((Character)sOp).charValue()));}
                } else if(fOpType==Type.INT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Integer)fOp).intValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Integer)fOp).intValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Integer)fOp).intValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Integer)fOp).intValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Integer)fOp).intValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Integer)fOp).intValue()/((Character)sOp).charValue()));}
                } else if(fOpType==Type.SHORT) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Short)fOp).shortValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Short)fOp).shortValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Short)fOp).shortValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Short)fOp).shortValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Short)fOp).shortValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Short)fOp).shortValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Short)fOp).shortValue()/((Character)sOp).charValue()));}
                } else if(fOpType==Type.BYTE) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Byte)fOp).byteValue()/((Character)sOp).charValue()));}
                } else if(fOpType==Type.CHAR) {
                    if(sOpType==Type.DOUBLE){setResult(this.calc.val_(((Character)fOp).charValue()/((Double)sOp).doubleValue()));}
                    if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Character)fOp).charValue()/((Float)sOp).floatValue()));}
                    if(sOpType==Type.LONG){setResult(this.calc.val_(((Character)fOp).charValue()/((Long)sOp).longValue()));}
                    if(sOpType==Type.INT){setResult(this.calc.val_(((Character)fOp).charValue()/((Integer)sOp).intValue()));}
                    if(sOpType==Type.SHORT){setResult(this.calc.val_(((Character)fOp).charValue()/((Short)sOp).shortValue()));}
                    if(sOpType==Type.BYTE){setResult(this.calc.val_(((Character)fOp).charValue()/((Byte)sOp).byteValue()));}
                    if(sOpType==Type.CHAR){setResult(this.calc.val_(((Character)fOp).charValue()/((Character)sOp).charValue()));}
                }
            } else if (operation==Operator.ANDBW) {
                if(fOpType==Type.INT) {
                    if(sOpType==Type.INT) {
                        setResult(this.calc.val_(((Integer)fOp).intValue()&((Integer)sOp).intValue()));
                    }
                } else if(fOpType==Type.LONG)
                    if(sOpType==Type.LONG){
                        setResult(this.calc.val_(((Long)fOp).longValue()&((Long)sOp).longValue()));
                    }
            } else if(operation==Operator.ORBW) {
                if(fOpType==Type.INT) {
                    if(sOpType==Type.INT){
                        setResult(this.calc.val_(((Integer)fOp).intValue()|((Integer)sOp).intValue()));
                    }
                } else if(fOpType==Type.LONG)
                    if(sOpType==Type.LONG){
                        setResult(this.calc.val_(((Long)fOp).longValue()|((Long)sOp).longValue()));
                    }
            } else if(operation==Operator.XORBW) {
                if(fOpType==Type.INT) {
                    if(sOpType==Type.INT){
                        setResult(this.calc.val_(((Integer)fOp).intValue()^((Integer)sOp).intValue()));
                    }
                } else if(fOpType==Type.LONG) {
                    if(sOpType==Type.LONG){
                        setResult(this.calc.val_(((Long)fOp).longValue()^((Long)sOp).longValue()));
                    }
                }
            } else if(operation==Operator.REM) {
                if(sOpType==Type.DOUBLE){setResult(this.calc.val_((((Double)fOp).doubleValue())%((Double)sOp).doubleValue()));}
                if(sOpType==Type.FLOAT){setResult(this.calc.val_(((Float)fOp).floatValue()%((Float)sOp).floatValue()));}
                if(sOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()%((Long)sOp).longValue()));}
                if(sOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()%((Integer)sOp).intValue()));}
                if(sOpType==Type.SHORT){setResult(this.calc.val_(((Short)fOp).shortValue()%((Short)sOp).shortValue()));}
                if(sOpType==Type.BYTE){setResult(this.calc.val_(((Byte)fOp).byteValue()%((Byte)sOp).byteValue()));}
                if(sOpType==Type.CHAR){setResult(this.calc.val_(((Character)fOp).charValue()%((Character)sOp).charValue()));}
            } else if(operation==Operator.SHL) {
                //TODO not to sure that this SHL implementation is correct
                if(fOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()<<((Integer)sOp).intValue()));}
                if(fOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()<<((Integer)sOp).intValue()));}
            } else if(operation==Operator.SHR) {
                if(fOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()>>((Integer)sOp).intValue()));}
                if(fOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()>>((Integer)sOp).intValue()));}
            } else if(operation==Operator.USHR) {
                if(fOpType==Type.LONG){setResult(this.calc.val_(((Long)fOp).longValue()>>>((Integer)sOp).intValue()));}
                if(fOpType==Type.INT){setResult(this.calc.val_(((Integer)fOp).intValue()>>>((Integer)sOp).intValue()));}
            } else if(operation==Operator.EQ) {
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.DOUBLE) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()==((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.FLOAT) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()==((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.LONG) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()==((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.INT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()==((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.SHORT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()==((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.BYTE) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()==((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.CHAR) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()==((Number)sOp).longValue()))); }
            } else if(operation==Operator.NE) {
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.DOUBLE) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()!=((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.FLOAT) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()!=((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.LONG) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()!=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.INT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()!=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.SHORT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()!=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.BYTE) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()!=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.CHAR) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()!=((Number)sOp).longValue()))); }
            } else if(operation==Operator.GT) {
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.DOUBLE) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()>((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.FLOAT) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()>((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.LONG) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.INT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.SHORT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.BYTE) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.CHAR) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>((Number)sOp).longValue()))); }
            } else if(operation==Operator.GE) {
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.DOUBLE) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()>=((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.FLOAT) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()>=((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.LONG) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.INT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.SHORT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.BYTE) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.CHAR) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()>=((Number)sOp).longValue()))); }
            } else if(operation==Operator.LT) {
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.DOUBLE) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()<((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.FLOAT) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()<((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.LONG) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.INT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.SHORT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.BYTE) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.CHAR) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<((Number)sOp).longValue()))); }
            } else if(operation==Operator.LE) {
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.DOUBLE) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()<=((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.FLOAT) { setResult(this.calc.val_((boolean)(((Number)fOp).doubleValue()<=((Number)sOp).doubleValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.LONG) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.INT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.SHORT) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.BYTE) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<=((Number)sOp).longValue()))); }
                if (Type.lub(firstOp.getType(), secondOp.getType())==Type.CHAR) { setResult(this.calc.val_((boolean)(((Number)fOp).longValue()<=((Number)sOp).longValue()))); }
            } else if(operation==Operator.AND) {
                setResult(this.calc.val_(((Boolean)fOp).booleanValue()&&((Boolean)sOp).booleanValue()));
            } else if(operation==Operator.OR) {
                setResult(this.calc.val_(((Boolean)fOp).booleanValue()||((Boolean)sOp).booleanValue()));
            }
        }
    } 

    private void applyConversion(Simplex toConvert, char to) 
    throws NoResultException {
        final char from = toConvert.getType();
        final Number n;
        if (from == Type.CHAR) {
            final char c = ((Character) toConvert.getActualValue()).charValue();
            n = Integer.valueOf((int) c);
        } else if (from == Type.BOOLEAN) {
            final boolean b = ((Boolean) toConvert.getActualValue()).booleanValue();
            n = Integer.valueOf(b ? 1 : 0);
        } else {
            n = (Number) toConvert.getActualValue();
        }
        if (to == Type.BOOLEAN) {
            setResult(this.calc.val_((n.intValue() == 0 ? false : true)));
        } else if (to == Type.BYTE) {
            setResult(this.calc.val_(n.byteValue()));
        } else if (to == Type.SHORT) {
            setResult(this.calc.val_(n.shortValue()));
        } else if (to == Type.INT) {
            setResult(this.calc.val_(n.intValue()));
        } else if (to == Type.LONG) {
            setResult(this.calc.val_(n.longValue()));
        } else if (to == Type.CHAR) {
            setResult(this.calc.val_((char) n.intValue()));
        } else if (to == Type.FLOAT) {
            setResult(this.calc.val_(n.floatValue()));
        } else if (to == Type.DOUBLE) {
            setResult(this.calc.val_(n.doubleValue()));
        } else {
            throw new NoResultException();
        }
    }
}
