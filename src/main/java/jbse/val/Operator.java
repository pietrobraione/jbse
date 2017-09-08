package jbse.val;

import jbse.common.Type;
import jbse.val.exc.InvalidTypeException;

/**
 * Enumeration of all the operators.
 */
public enum Operator {
	//Operators implemented by JVM bytecodes

	/** Arithmetic sum */
    ADD("+", true, false, false, 0) {
		@Override public Operator twist() { return ADD; }
	},
    
	/** Arithmetic subtraction */
    SUB("-", true, false, false, 0) {
		@Override public Operator twist() { return null; }
	},

	/** Arithmetic multiplication */
    MUL("*", true, false, false, 1) {
		@Override public Operator twist() { return MUL; }
	},

	/** Arithmetic division */
    DIV("/", true, false, false, 1) {
		@Override public Operator twist() { return null; }
	},

    /** Arithmetic remainder */
    REM("%", true, false, false, 1) {
		@Override public Operator twist() { return null; }
	},

    /** Arithmetic negation */
    NEG("~", false, false, false, 3) {
		@Override public Operator twist() { return null; }
	},
    
    /** Left shift */
    SHL("<<", true, false, false, -1) {
		@Override public Operator twist() { return null; }
	},

    /** Arithmetic right shift */
    SHR(">>", true, false, false, -1) {
		@Override public Operator twist() { return null; }
	},

    /** Logical right shift */
    USHR(">>>", true, false, false, -1) {
		@Override public Operator twist() { return null; }
	},
    
    /** Bitwise OR */
    ORBW ("|", true, false, false, -6) {
		@Override public Operator twist() { return ORBW; }
	},
    
    /** Bitwise AND */
    ANDBW ("&", true, false, false, -4) {
		@Override public Operator twist() { return ANDBW; }
	},
    
    /** Bitwise XOR */
    XORBW ("^", true, false, false, -5) {
		@Override public Operator twist() { return XORBW; }
	},
    
    /** Arithmetic greater-or-equal-than comparison */
    GE  (">=", true, false, true, -2) {
		@Override public Operator twist() { return LE; }
	},
    
	//Java language operators not implemented by bytecodes but necessary for symbolic reasoning
	
    /** Logical AND */
    AND ("&&", true, true, true, -7) {
		@Override public Operator twist() { return AND; }
	},

    /** Logical OR */
    OR  ("||", true, true, true, -8) {
		@Override public Operator twist() { return OR; }
	},

    /** Logical NOT */
    NOT ("!", false, true, true, 2) {
		@Override public Operator twist() { return null; }
	},

    /** Inequality comparison */
    NE  ("!=", true, false, true, -3) {
		@Override public Operator twist() { return NE; }
	},

    /** Equality comparison */
    EQ  ("==", true, false, true, -3) {
		@Override public Operator twist() { return EQ; }
	},

    /** Arithmetic less-or-equal-than comparison */
    LE  ("<=", true, false, true, -2) {
		@Override public Operator twist() { return GE; }
	},

    /** Arithmetic greater-than comparison */
    GT  (">", true, false, true, -2) {
		@Override public Operator twist() { return LT; }
	},

    /** Arithmetic less-than comparison */
    LT  ("<", true, false, true, -2) {
		@Override public Operator twist() { return GT; }
	};

    private final String toString;
    private final boolean isBinary;
    private final boolean acceptsBoolean;
    private final boolean returnsBoolean;
    private final int precedence;
    
    private Operator(String toString, boolean isBinary, boolean acceptsBoolean, boolean returnsBoolean, int precedence) {
    	this.toString = toString;
    	this.isBinary = isBinary;
    	this.acceptsBoolean = acceptsBoolean;
    	this.returnsBoolean = returnsBoolean;
    	this.precedence = precedence;
    }
    
	public boolean isBinary() {
		return this.isBinary;
	}
	
	public boolean acceptsBoolean() {
		return this.acceptsBoolean;
	}
	
	public boolean returnsBoolean() {
		return this.returnsBoolean;
	}
	
	public int precedence() {
		return this.precedence;
	}
	
	/**
	 * Twists a binary operator.
	 * 
	 * @return the operator such that 
	 *         {@code x op y == y op.twist() x},
	 *         or {@code null} if no such operator
	 *         exists or this operator is unary.
	 */
	public abstract Operator twist(); //must be defined by methods 

	public static void typeCheck(Operator operator, char operandType) throws InvalidTypeException {
        if (operandType == Type.UNKNOWN || Type.isReference(operandType)) {
        	throw new InvalidTypeException("operand has a nonprimitive type " + operandType);
        } 
        //arithmetic negation must have a primitive operand
        //(see JVM spec 2nd ed. sec. 3.11.1 tab 3.2, and corresponding bytecodes specification)        	
        if (operator == Operator.NEG && !Type.isPrimitiveOpStack(operandType)) {
        	throw new InvalidTypeException("ill-typed arithmetic negation; operand has type " + operandType);
        }
        //for logical operations we liberally accept every integral type operand
        if (operator == Operator.NOT && !Type.isPrimitiveIntegral(operandType)) {
        	throw new InvalidTypeException("ill-typed logical negation; operand has type " + operandType);
        }
	}
	
	public static void typeCheck(Operator operation, char firstOperandType, char secondOperandType) 
	throws InvalidTypeException {
        if (firstOperandType == Type.UNKNOWN || Type.isReference(firstOperandType)) {
        	throw new InvalidTypeException("first operand has a nonprimitive type " + firstOperandType);
        }
        if (secondOperandType == Type.UNKNOWN || Type.isReference(secondOperandType)) {
        	throw new InvalidTypeException("second operand has a nonprimitive type " + secondOperandType);
        } 
        //add, sub, mul, div, rem operands must have same type and must be primitive 
        //(see JVM spec 2nd ed. sec. 3.11.1 tab 3.2, and corresponding bytecodes specification)
        if ((operation == Operator.ADD ||
        		operation == Operator.SUB ||
        		operation == Operator.MUL ||
        		operation == Operator.DIV ||
        		operation == Operator.REM) && (firstOperandType != secondOperandType || !Type.isPrimitiveOpStack(firstOperandType))) {
        	throw new InvalidTypeException("ill-typed sum, subtraction, multiplication, division or remainder; first operand has type " + firstOperandType + ", second operand has type " + secondOperandType);
        }
        //shift operators must have integral first operand and int (not long) second operand
        //(see JVM spec 2nd ed. sec. 3.11.1 tab 3.2, and corresponding bytecodes specification)
        if ((operation == Operator.SHL ||
        		operation == Operator.SHR ||
        		operation == Operator.USHR) && (!Type.isPrimitiveIntegralOpStack(firstOperandType) || secondOperandType != Type.INT)) {
        	throw new InvalidTypeException("ill-typed shift; first operand has type " + firstOperandType + ", second operand has type " + secondOperandType);
        }
        //bitwise operators operands must have same type and must be integral
        //(see JVM spec 2nd ed. sec. 3.11.1 tab 3.2, and corresponding bytecodes specification)
        if ((operation == Operator.ANDBW ||
        		operation == Operator.ORBW ||
        		operation == Operator.XORBW) && (firstOperandType != secondOperandType || !Type.isPrimitiveIntegralOpStack(firstOperandType))) {
        	throw new InvalidTypeException("ill-typed bitwise operation; first operand has type " + firstOperandType + ", second operand has type " + secondOperandType);
        }
        //for logical operations we liberally accept every integral type operands
        if ((operation == Operator.AND ||
        		operation == Operator.OR) && (!Type.isPrimitiveIntegral(firstOperandType) || !Type.isPrimitiveIntegral(secondOperandType))) {
        	throw new InvalidTypeException("ill-typed logical operation; first operand has type " + firstOperandType + ", second operand has type " + secondOperandType);
        }
        //for comparisons we liberally accept every comparison between primitive types, or between references
        if ((operation == Operator.EQ ||
        		operation == Operator.NE ||
        		operation == Operator.GT ||
        		operation == Operator.GE ||
        		operation == Operator.LT ||
        		operation == Operator.LE) && (Type.isPrimitive(firstOperandType) != Type.isPrimitive(secondOperandType))) {
        	throw new InvalidTypeException("ill-typed comparison; first operand has type " + firstOperandType + ", second operand has type " + secondOperandType);
        }
	}
	
    @Override
    public String toString() {
    	return this.toString;
    }

    /**
     * Calculates the return type of this operator when applied to its operands.
     * Works only if {@code this} operator and its operands typecheck.
     * 
     * @param typeFirstOperand a {@code char}, the type of the first operand, or {@code null}
     *        if {@code !}{@link #isBinary()}.
     * @param typeSecondOperand a {@code char}, the type of the second operand.
     * @return a {@code char}, the type of the result of the application of {@code this}
     *         operator to arguments of the prescribed type.
     */
	public char returnType(char typeFirstOperand, char typeSecondOperand) {
		if (this.returnsBoolean()) { 
			return Type.BOOLEAN;
		}
		//TODO that's wrong!!! well, at least with some operators like shifts
		return Type.lub(typeFirstOperand, typeSecondOperand);	
	}
}