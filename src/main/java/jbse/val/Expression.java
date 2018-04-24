package jbse.val;

import jbse.common.Type;
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

    /**
     * Factory method for verbatim expressions (binary).
     * 
     * @param calc a {@link Calculator}.
     * @param firstOperand a {@link Primitive}, the first operand.
     * @param operator an {@link Operator}.
     * @param secondOperand a {@link Primitive}, the second operand.
     * @return an {@link Expression}.
     * @throws InvalidOperandException if {@code firstOperand} or {@code secondOperand}
     *         is {@code null}.
     * @throws InvalidOperatorException if {@code operator} is not binary. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public static Expression makeExpressionBinary(Calculator calc, Primitive firstOperand, Operator operator, Primitive secondOperand) 
    throws InvalidOperandException, InvalidOperatorException, InvalidTypeException {
        //checks on parameters
        if (firstOperand == null) {
            throw new InvalidOperandException("no first operand in binary expression construction");
        }
        if (operator == null) {
            throw new InvalidOperatorException("no operator in binary expression construction");
        }
        if (secondOperand == null) {
            throw new InvalidOperandException("no second operand in binary expression construction");
        }
        if (!operator.isBinary()) {
            throw new InvalidOperatorException("operator " + operator + " is not binary");
        }
        Operator.typeCheck(operator, firstOperand.getType(), secondOperand.getType());

        //calculates the default type
        final char defaultType = operator.returnType(firstOperand.getType(), secondOperand.getType()); 

        return new Expression(defaultType, calc, firstOperand, operator, secondOperand);
    }

    /**
     * Factory method for verbatim expressions (unary).
     * 
     * @param calc a {@link Calculator}.
     * @param operator an {@link Operator}.
     * @param operand a {@link Primitive}, the operand.
     * @return an {@link Expression}.
     * @throws InvalidOperandException if {@code operand} 
     *         is {@code null}.
     * @throws InvalidOperatorException if {@code operator} is not unary. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public static Expression makeExpressionUnary(Calculator calc, Operator operator, Primitive operand) 
    throws InvalidOperatorException, InvalidOperandException, InvalidTypeException {
        if (operator == null) {
            throw new InvalidOperatorException("no operator in unary expression construction");
        }
        if (operand == null) {
            throw new InvalidOperandException("no operand in unary expression construction");
        }
        if (operator.isBinary()) {
            throw new InvalidOperatorException("operator " + operator + " is not unary");
        }
        Operator.typeCheck(operator, operand.getType());

        //calculates the default type
        final char defaultType = (operator.returnsBoolean() ? Type.BOOLEAN : operand.getType());

        return new Expression(defaultType, calc, null, operator, operand);
    }

    /**
     * Constructor.
     * 
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    private Expression(char type, Calculator calc, Primitive firstOperand, Operator operator, Primitive secondOperand) 
    throws InvalidTypeException {
    	super(type, null, calc); //TODO put sensible history point?
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
    }
    
    private String stringify(boolean toString) {
        final StringBuilder retVal = new StringBuilder();
        boolean parentheses = false;
        if (this.firstOp != null) {
            if (this.firstOp instanceof Expression) {
                parentheses = true; //default
                final Operator firstOpOperator = ((Expression) this.firstOp).operator;
                if (firstOpOperator.precedence() >= operator.precedence()) {
                    parentheses = false;
                } 
            }
            retVal.append(parentheses ? "(" : "");
            retVal.append((toString || !this.firstOp.isSymbolic()) ? this.firstOp.toString() : ((Symbolic) this.firstOp).asOriginString());
            retVal.append(parentheses ? ")" : "");
        }
        retVal.append(" ");
        retVal.append(this.operator.toString());
        retVal.append(" ");
        parentheses = false;
        if (this.secondOp instanceof Expression) {
            parentheses = true; //default
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
     * Gets the first operand of the {@link Expression} if exists.
     * 
     * @return a {@link Primitive} if {@code !this.}{@link #isUnary()}, 
     *         {@code null} otherwise.
     */
    public Primitive getFirstOperand() {
        return this.firstOp;
    }

    /**
     * Returns the second operand of expression if exists.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Primitive doReplace(Primitive from, Primitive to) {
        final Primitive first;
        if (isUnary()) {
            first = null;
        } else if (this.firstOp.equals(from)) {
            first = to;
        } else if (this.firstOp instanceof PrimitiveSymbolicComputed) {
            first = ((PrimitiveSymbolicComputed) this.firstOp).doReplace(from, to);
        } else {
            first = this.firstOp;
        }

        final Primitive second;
        if (this.secondOp.equals(from)) {
            second = to;
        } else if (this.secondOp instanceof PrimitiveSymbolicComputed) {
            second = ((PrimitiveSymbolicComputed) this.secondOp).doReplace(from, to);
        } else {
            second = this.secondOp;
        }

        try {
            if (isUnary()) {
                return this.calc.applyUnary(this.operator, second); //TODO possible bug! Here rewriting is applied!
            } else {
                return this.calc.applyBinary(first, this.operator, second);//TODO possible bug! Here rewriting is applied!
            }
        } catch (InvalidOperatorException | InvalidTypeException | InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    @Override
    public String asOriginString() {
        return stringify(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(PrimitiveVisitor v) throws Exception {
        v.visitExpression(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.toString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * {@inheritDoc}
     */
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