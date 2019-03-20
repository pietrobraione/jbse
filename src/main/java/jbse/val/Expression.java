package jbse.val;

import static jbse.val.HistoryPoint.unknown;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class representing the {@code PrimitiveSymbolicComputed} obtained 
 * by applying a unary or binary {@link Operator} to arguments.
 */
public final class Expression extends PrimitiveSymbolicComputed {
    /** The operator. */
    private final Operator operator;

    /** The first operand (null if operator is unary). */
    private final Primitive firstOp;

    /** The second operand (or the only operand if operator is unary). */
    private final Primitive secondOp;

    /** The hash code of this object. */
    private final int hashCode;

    /** The string representation of this object. */
    private final String toString;
    
    /** The origin String representation of this object. */
    private final String asOriginString;

    /**
     * Factory method for verbatim expressions (binary).
     * 
     * @param firstOperand a {@link Primitive}, the first operand.
     * @param operator an {@link Operator}.
     * @param secondOperand a {@link Primitive}, the second operand.
     * @return an {@link Expression}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}.
     * @throws InvalidOperatorException if {@code operator == null} or is not binary. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public static Expression makeExpressionBinary(Primitive firstOperand, Operator operator, Primitive secondOperand) 
    throws InvalidOperandException, InvalidOperatorException, InvalidTypeException {
        //checks on parameters
        if (firstOperand == null) {
            throw new InvalidOperandException("No first operand in binary expression construction.");
        }
        if (operator == null) {
            throw new InvalidOperatorException("No operator in binary expression construction.");
        }
        if (secondOperand == null) {
            throw new InvalidOperandException("No second operand in binary expression construction.");
        }
        if (!operator.isBinary()) {
            throw new InvalidOperatorException("Operator " + operator + " is not binary.");
        }
        Operator.typeCheck(operator, firstOperand.getType(), secondOperand.getType());

        //calculates the expression type
        final char type = operator.returnType(firstOperand.getType(), secondOperand.getType()); 

        try {
			return new Expression(type, firstOperand, operator, secondOperand);
		} catch (InvalidInputException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Factory method for verbatim expressions (unary).
     * 
     * @param operator an {@link Operator}.
     * @param operand a {@link Primitive}, the operand.
     * @return an {@link Expression}.
     * @throws InvalidOperandException if {@code operand == null}.
     * @throws InvalidOperatorException if {@code operator} is not unary. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public static Expression makeExpressionUnary(Operator operator, Primitive operand) 
    throws InvalidOperatorException, InvalidOperandException, InvalidTypeException {
        if (operator == null) {
            throw new InvalidOperatorException("No operator in unary expression construction.");
        }
        if (operand == null) {
            throw new InvalidOperandException("No operand in unary expression construction.");
        }
        if (operator.isBinary()) {
            throw new InvalidOperatorException("Operator " + operator + " is not unary.");
        }
        Operator.typeCheck(operator, operand.getType());

        //calculates the expression type
        final char type = (operator.returnsBoolean() ? Type.BOOLEAN : operand.getType());

        try {
			return new Expression(type, null, operator, operand);
		} catch (InvalidInputException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Constructor.
     * 
     * @param type the type of the represented value.
     * @param firstOperand a {@link Primitive}, the first operand.
     * @param operator an {@link Operator}.
     * @param secondOperand a {@link Primitive}, the second operand.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException never.
     */
    private Expression(char type, Primitive firstOperand, Operator operator, Primitive secondOperand) 
    throws InvalidTypeException, InvalidInputException {
    	super(type, unknown()); //TODO put sensible history point?
        this.firstOp = firstOperand;
        this.operator = operator;
        this.secondOp = secondOperand;
        
        //calculates hashCode
    	final int prime = 271;
    	int tmpHashCode = 1;
    	tmpHashCode = prime * tmpHashCode + ((this.firstOp == null) ? 0 : this.firstOp.hashCode());
    	tmpHashCode = prime * tmpHashCode + this.operator.hashCode();
    	tmpHashCode = prime * tmpHashCode + this.secondOp.hashCode();
    	this.hashCode = tmpHashCode;

    	//calculates toString
    	this.toString = stringify(true);
    	
    	//calculates originString
    	this.asOriginString = stringify(false);
    }
    
    private String stringify(boolean toString) {
        final StringBuilder retVal = new StringBuilder();
        boolean parentheses = true; //default
        if (this.firstOp != null) {
            if (this.firstOp instanceof Expression) {
                final Operator firstOpOperator = ((Expression) this.firstOp).operator;
                if (toString && firstOpOperator.precedence() >= operator.precedence()) {
                    parentheses = false;
                } 
            }
            retVal.append(parentheses ? "(" : "");
            retVal.append((toString || !this.firstOp.isSymbolic()) ? this.firstOp.toString() : ((Symbolic) this.firstOp).asOriginString());
            retVal.append(parentheses ? ")" : "");
        }
        if (toString) {
        	retVal.append(" ");
        }
        retVal.append(this.operator.toString());
        if (toString) {
        	retVal.append(" ");
        }
        parentheses = true; //default
        if (this.secondOp instanceof Expression) {
            if (toString) {
            	final Operator secondOpOperator = ((Expression) this.secondOp).operator;
            	if (secondOpOperator.precedence() > this.operator.precedence()) {
            		parentheses = false;
            	} else if (secondOpOperator.precedence() == this.operator.precedence()) {
            		if (secondOpOperator == this.operator) {
            			if (this.operator == Operator.ADD || this.operator == Operator.SUB || this.operator == Operator.MUL) {
            				parentheses = false;
            			}
            		} else if (operator == Operator.MUL && secondOpOperator == Operator.DIV) {
            			parentheses = false;
            		}
            	} else if (operator == Operator.ADD && secondOpOperator == Operator.SUB) {
            		parentheses = false;
            	} else if (operator == Operator.SUB && secondOpOperator == Operator.ADD) {
            		parentheses = false;
            	}
            }
        }               
        retVal.append(parentheses ? "(" : "");
        retVal.append((toString || !this.secondOp.isSymbolic()) ? this.secondOp.toString() : ((Symbolic) this.secondOp).asOriginString());
        retVal.append(parentheses ? ")" : "");
        return retVal.toString();
    }

    /**
     * Checks whether the expression is unary.
     * 
     * @return {@code true} iff this expression is unary.
     */
    public boolean isUnary() {
        return (this.firstOp == null);
    }

    /**
     * Gets the operand of a unary {@link Expression}.
     * 
     * @return a {@link Primitive}.
     */
    public Primitive getOperand() {
        return this.secondOp;
    }

    /**
     * Gets the first operand of this {@link Expression} if exists.
     * 
     * @return a {@link Primitive} if {@code !this.}{@link #isUnary()}, 
     *         {@code null} otherwise.
     */
    public Primitive getFirstOperand() {
        return this.firstOp;
    }

    /**
     * Returns the second operand of this {@link Expression}
     * (the only operand if {@code this.}{@link #isUnary()}).
     * 
     * @return a {@link Primitive}.
     */
    public Primitive getSecondOperand() {
        return this.secondOp;
    }

    /**
     * Gets the operator.
     */
    public Operator getOperator() {
        return this.operator;
    }
    
    @Override
    public String asOriginString() {
        return this.asOriginString;
    }
    
    @Override
    public Symbolic root() {
    	return this;
    }
    
    @Override
    public boolean hasContainer(Symbolic s) {
		if (s == null) {
			throw new NullPointerException();
		}
		return equals(s);
    }

    @Override
    public void accept(PrimitiveVisitor v) throws Exception {
        v.visitExpression(this);
    }

    @Override
    public String toString() {
        return this.toString;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Expression other = (Expression) obj;
        if (this.firstOp == null) {
            if (other.firstOp != null) {
                return false;
            }
        } else if (!this.firstOp.equals(other.firstOp)) {
            return false;
        }
        if (this.operator != other.operator) {
            return false;
        }
        if (this.secondOp == null) {
            if (other.secondOp != null) {
                return false;
            }
        } else if (!this.secondOp.equals(other.secondOp)) {
            return false;
        }
        return true;
    }
}