package jbse.mem;

import java.util.HashSet;
import java.util.Set;

import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidOperatorException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.ValueDoesNotSupportNativeException;

/**
 * Class that represent a unary or binary expression.
 */
public class Expression extends Primitive {
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
    	super(type, calc);
        this.firstOp = firstOperand;
        this.operator = operator;
        this.secondOp = secondOperand;
        
        //calculates hashCode
    	final int prime = 271;
    	int tmpHashCode = 1;
    	tmpHashCode = prime * tmpHashCode + ((firstOp == null) ? 0 : firstOp.hashCode());
    	tmpHashCode = prime * tmpHashCode + operator.hashCode();
    	tmpHashCode = prime * tmpHashCode + secondOp.hashCode();
    	this.hashCode = tmpHashCode;

    	//calculates toString
		String tmpToString = "";
        boolean parentheses = false;
        if (firstOp != null) {
        	if (firstOp instanceof Expression) {
        		parentheses = true; //default
    			final Operator firstOpOperator = ((Expression) firstOp).operator;
        		if (firstOpOperator.precedence() >= operator.precedence()) {
        			parentheses = false;
        		} 
        	}
    		tmpToString += (parentheses ? "(" : "") + firstOp.toString() + (parentheses ? ")" : "");
        }
        tmpToString += " " + operator.toString() + " ";
        parentheses = false;
		if (secondOp instanceof Expression) {
			parentheses = true; //default
			Operator secondOpOperator = ((Expression) secondOp).operator;
			if (secondOpOperator.precedence() > operator.precedence()) {
				parentheses = false;
			} else if (secondOpOperator.precedence() == operator.precedence()) {
				if (secondOpOperator == operator) {
					if (operator == Operator.ADD || operator == Operator.SUB || operator == Operator.MUL) {
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
		tmpToString += (parentheses ? "(" : "") + secondOp.toString() + (parentheses ? ")" : "");
        this.toString = tmpToString;
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
        return(firstOp);
    }
    
    /**
     * Returns the second operand of expression if exists.
     */
    public Primitive getSecondOperand() {
        return(secondOp);
    }
    
    /**
     * Gets the operator.
     */
    public Operator getOperator() {
        return(operator);
    }

	/**
	 * Returns a new {@link Primitive} built by substitution of 
	 * a subterm with another one.
	 * 
	 * @param from a {@link Primitive}, to be replaced. 
	 * @param to a {@link Primitive}, replacing {@code from}.
	 * @return the {@link Primitive} obtained by replacing 
	 *         every occurrence of {@code from} in {@code this}
	 *         with {@code to}.
	 * @throws InvalidOperandException if {@code from} or {@code to}
	 *         is {@code null}.
	 * @throws InvalidTypeException if {@code from} and {@code to}
	 *         have different type. 
	 */
	public Primitive replace(Primitive from, Primitive to) 
	throws InvalidOperandException, InvalidTypeException {
		if (from == null || to == null) {
			throw new InvalidOperandException("one parameter of replace is null");
		}
		if (from.getType() != to.getType()) {
			throw new InvalidTypeException("cannot replace a primitive with type " + from.getType() + " with one with type " + to.getType());
		}
		if (from.equals(to)) {
			return this;
		}
		
		final Primitive first;
		if (isUnary()) {
			first = null;
		} else if (this.firstOp.equals(from)) {
			first = to;
		} else if (this.firstOp instanceof Expression) {
			first = ((Expression) this.firstOp).replace(from, to);
		} else {
			first = this.firstOp;
		}
		
		final Primitive second;
		if (this.secondOp.equals(from)) {
			second = to;
		} else if (this.secondOp instanceof Expression) {
			second = ((Expression) this.secondOp).replace(from, to);
		} else {
			second = this.secondOp;
		}
		
		try {
			if (isUnary()) {
				return this.calc.applyUnary(this.operator, second); //TODO possible bug! Here rewriting is applied!
			} else {
				return this.calc.applyBinary(first, this.operator, second);//TODO possible bug! Here rewriting is applied!
			}
		} catch (InvalidOperatorException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	/**
	 * Returns the set of all the identifiers of symbolic 
	 * values mentioned in this {@link Expression}.
	 * 
	 * @return a {@link Set}<code>&lt;</code>{@link Integer}<code>&gt;</code> of all and only the 
	 *         identifiers of symbolic values mentioned 
	 *         in this expression (empty when none).
	 */
	public Set<Integer> symIds() {
		HashSet<Integer> retVal = new HashSet<Integer>();

		if (this.firstOp == null) {
			//nothing to do
		} else if (this.firstOp instanceof PrimitiveSymbolic) {
			retVal.add(((PrimitiveSymbolic) this.firstOp).getId());
		} else if (this.firstOp instanceof Expression) {
			retVal.addAll(((Expression) this.firstOp).symIds());
		} else if (this.firstOp instanceof FunctionApplication) {
			retVal.addAll(((FunctionApplication) this.firstOp).symIds());
		}
		
		if (this.secondOp instanceof PrimitiveSymbolic) {
			retVal.add(((PrimitiveSymbolic) this.secondOp).getId());
		} else if (this.secondOp instanceof Expression) {
			retVal.addAll(((Expression) this.secondOp).symIds());
		} else if (this.secondOp instanceof FunctionApplication) {
			retVal.addAll(((FunctionApplication) this.secondOp).symIds());
		}
		
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean surelyTrue() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean surelyFalse() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public boolean isSymbolic() {
    	return (this.secondOp.isSymbolic() || (this.firstOp == null ? false : this.firstOp.isSymbolic()));
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
	public Expression clone() {
		return (Expression) super.clone();
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
		Expression other = (Expression) obj;
		if (firstOp == null) {
			if (other.firstOp != null) {
				return false;
			}
		} else if (!firstOp.equals(other.firstOp)) {
			return false;
		}
		if (operator != other.operator) {
			return false;
		}
		if (secondOp == null) {
			if (other.secondOp != null) {
				return false;
			}
		} else if (!secondOp.equals(other.secondOp)) {
			return false;
		}
		return true;
	}
}