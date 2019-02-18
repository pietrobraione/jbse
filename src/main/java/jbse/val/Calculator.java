package jbse.val;

import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.INT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.narrows;
import static jbse.common.Type.NULLREF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.SHORT;
import static jbse.common.Type.widens;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

public abstract class Calculator {
    /** The (only) {@link Any} value. */
    private final Any ANY;

    /** The boolean {@code true} value. */
    private final Simplex TRUE; 

    /** The boolean {@code false} value. */
    private final Simplex FALSE;

    /** The int 0 value. */
    private final Simplex INT_ZERO;

    /** Default value for primitive type {@code boolean}. */
    private final Simplex DEFAULT_BOOL;

    /** Default value for primitive type {@code byte}. */
    private final Simplex DEFAULT_BYTE;

    /** Default value for primitive type {@code short}. */
    private final Simplex DEFAULT_SHORT;

    /** Default value for primitive type {@code int}. */
    private final Simplex DEFAULT_INT;

    /** Default value for primitive type {@code long}. */
    private final Simplex DEFAULT_LONG;

    /** Default value for primitive type {@code float}. */
    private final Simplex DEFAULT_FLOAT;

    /** Default value for primitive type {@code double}. */
    private final Simplex DEFAULT_DOUBLE;

    /** Default value for primitive type {@code char}. */
    private final Simplex DEFAULT_CHAR;

    /** Default value for reference types. */
    private final ReferenceConcrete DEFAULT_REFERENCE;

    public Calculator() {
        try {
            this.ANY               = Any.make(this);
            this.TRUE              = Simplex.make(this, Boolean.valueOf(true));
            this.FALSE             = Simplex.make(this, Boolean.valueOf(false));
            this.INT_ZERO          = Simplex.make(this, Integer.valueOf(0));
            this.DEFAULT_BOOL      = this.FALSE;
            this.DEFAULT_BYTE      = Simplex.make(this, Byte.valueOf((byte) 0));
            this.DEFAULT_SHORT     = Simplex.make(this, Short.valueOf((short) 0));
            this.DEFAULT_INT       = this.INT_ZERO;
            this.DEFAULT_LONG      = Simplex.make(this, Long.valueOf(0L));
            this.DEFAULT_FLOAT     = Simplex.make(this, Float.valueOf(0.0f));
            this.DEFAULT_DOUBLE    = Simplex.make(this, Double.valueOf(0.0d));
            this.DEFAULT_CHAR      = Simplex.make(this, Character.valueOf('\u0000'));
            this.DEFAULT_REFERENCE = Null.getInstance();
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for values with type {@link Any}.
     * 
     * @return an {@link Any}.
     */
    public Any valAny() {
        return this.ANY;
    }

    /**
     * Factory method for concrete values with type boolean. 
     * 
     * @param value a {@code boolean}.
     * @return a {@link Simplex} representing {@code value}.
     */
    public Simplex valBoolean(boolean value) {
        return (value ? this.TRUE : this.FALSE);
    }

    /**
     * Factory method for concrete values with type boolean. 
     * 
     * @param value a {@code boolean}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valByte(byte value) {
        try {
            return Simplex.make(this, Byte.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for concrete values with type short. 
     * 
     * @param value a {@code short}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valShort(short value) {
        try {
            return Simplex.make(this, Short.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for concrete values with type int. 
     * 
     * @param value an {@code int}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valInt(int value) {
        if (value == 0) {
            return this.INT_ZERO;
        }
        try {
            return Simplex.make(this, Integer.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for concrete values with type long. 
     * 
     * @param value a {@code long}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valLong(long value) {
        try {
            return Simplex.make(this, Long.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for concrete values with type float. 
     * 
     * @param value a {@code float}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valFloat(float value) {
        try {
            return Simplex.make(this, Float.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for concrete values with type double. 
     * 
     * @param value a {@code double}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valDouble(double value) {
        try {
            return Simplex.make(this, Double.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for concrete values with type char. 
     * 
     * @param value a {@code char}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public Simplex valChar(char value) {
        try {
            return Simplex.make(this, Character.valueOf(value));
        } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for {@link Term}s.
     * 
     * @param id an {@code int}, the identifier of the symbol.
     * @param type a {@code char} representing the type of the symbol 
     *        (see {@link Type}).
     * @param value a {@link String} representing the conventional
     *        value of the term.
     * @return a {@link Term}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    public Term valTerm(char type, String value) throws InvalidTypeException {
        try {
			return new Term(type, this, value);
		} catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    //same methods overloaded

    public Simplex val_(double val) {
        return this.valDouble(val);
    }

    public Simplex val_(float val) {
        return this.valFloat(val);
    }

    public Simplex val_(long val) {
        return this.valLong(val);
    }

    public Simplex val_(int val) {
        return this.valInt(val);
    }

    public Simplex val_(short val) {
        return this.valShort(val);
    }

    public Simplex val_(byte val) {
        return this.valByte(val);
    }

    public Simplex val_(char val) {
        return this.valChar(val);
    }

    public Simplex val_(boolean val) {
        return this.valBoolean(val);
    }

    public Simplex val_(Object v) {
        if (v instanceof Byte) {
            return valByte(((Byte) v).byteValue());
        } else if (v instanceof Short) {
            return valShort(((Short) v).shortValue());
        } else if (v instanceof Integer) {
            return valInt(((Integer) v).intValue());
        } else if (v instanceof Long) {
            return valLong(((Long) v).longValue());
        } else if (v instanceof Float) {
            return valFloat(((Float) v).floatValue());
        } else if (v instanceof Double) {
            return valDouble(((Double) v).doubleValue());
        } else if (v instanceof Boolean) {
            return valBoolean(((Boolean) v).booleanValue());
        } else if (v instanceof Character) {
            return valChar(((Character) v).charValue());
        } else {
            return null; //TODO raise InvalidTypeException?
        }
    }

    /**
     * Creates a default {@link Value} for a given type, 
     * as from JVM specification (see JVMS v8, 
     * section 2.3 and section 2.4).
     *  
     * @param type a {@code char}.
     * @return a new instance of {@link Value}, the default for {@code type}
     *         (either a {@link Simplex} or {@link Null#getInstance()}), 
     *         or {@code null} if {@code type} does not 
     *         indicate a primitive or reference type.
     */
    public Value createDefault(char type) {
        switch (type) {
        case BYTE:
            return this.DEFAULT_BYTE;
        case SHORT:
            return this.DEFAULT_SHORT;
        case INT:
            return this.DEFAULT_INT;
        case LONG:
            return this.DEFAULT_LONG;
        case FLOAT:
            return this.DEFAULT_FLOAT;
        case DOUBLE:
            return this.DEFAULT_DOUBLE;
        case CHAR:
            return this.DEFAULT_CHAR;
        case BOOLEAN:
            return this.DEFAULT_BOOL;
        case ARRAYOF:
        case NULLREF:
        case REFERENCE:
            return this.DEFAULT_REFERENCE;
        default: //Type.VOID
            return null;
        }
    }

    /**
     * Calculates the arithmetic sum of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand + secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive add(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic multiplication of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand * secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive mul(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic subtraction between two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand - secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive sub(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic division of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand / secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive div(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic remainder of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand % secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive rem(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic negation of a {@link Primitive}.
     * 
     * @param operand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code -operand}.
     * @throws InvalidOperandException if {@code operand == null}.
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive neg(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the bitwise AND of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand & secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive andBitwise(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the bitwise OR of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand | secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive orBitwise(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the bitwise XOR of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand ^ secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive xorBitwise(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the logical AND of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand && secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive and(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the logical OR of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand || secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive or(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the logical NOT of a {@link Primitive}.
     * 
     * @param operand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code !operand}.
     * @throws InvalidOperandException if {@code operand == null}.
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive not(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the left shift of a {@link Primitive}.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand << secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive shl(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic right shift of a {@link Primitive}.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand >> secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive shr(Primitive firstOperand,
                                  Primitive secondOperand) throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the logical right shift of a {@link Primitive}.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand >>> secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive ushr(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the equality comparison of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand == secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive eq(Primitive firstOperand, Primitive secondOperand) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the inequality comparison of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand != secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive ne(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic less-or-equal-than comparison 
     * of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand <= secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive le(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic less-than comparison
     * of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand < secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive lt(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic greater-or-equal-than comparison
     * of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand >= secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive ge(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Calculates the arithmetic greater-than comparison
     * of two {@link Primitive}s.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing {@code firstOperand > secondOperand}.
     * @throws InvalidOperandException if {@code firstOperand == null || secondOperand == null}. 
     * @throws InvalidTypeException if the expression cannot be typed.
     */
    public abstract Primitive gt(Primitive firstOperand, Primitive secondOperand)
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Converts a primitive value to another wider type.
     * 
     * @param type a {@code char} representing the type to which the
     *        value must be converted.
     * @param arg a {@link Primitive}.
     * @return a {@link Primitive} representing the result of converting 
     *         {@code arg} to {@code type}, by applying a widening conversion.
     * @throws InvalidOperandException when {@code arg == null}.
     * @throws InvalidTypeException when {@code arg} cannot be widened 
     *         to {@code type}, or {@code type} is not a valid primitive type. 
     */
    public abstract Primitive widen(char type, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Converts a primitive value to another narrower type.
     * 
     * @param type a {@code char} representing the type of the conversion.
     * @param arg a {@link Primitive}.
     * @return a {@link Primitive} representing the result of converting 
     *         {@code arg} to {@code type}, by applying a narrowing conversion.
     * @throws InvalidOperandException when {@code arg == null}.
     * @throws InvalidTypeException when {@code arg} cannot be narrowed 
     *         to {@code type}, or {@code type} is not a valid primitive type. 
     */
    public abstract Primitive narrow(char type, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException;

    /**
     * Applies a function to some arguments. The function returns a {@link Primitive} value.
     *  
     * @param type a {@code char} representing the type of the return value of {@code operator}
     *        (see {@link Type}).
     * @param historyPoint the current {@link HistoryPoint}.
     * @param operator a {@code String} representing the function to be applied.
     * @param args a {@link Value}{@code[]} representing the arguments to the function.
     * @return a {@link Primitive} representing {@code operator(args)}.
     * @throws InvalidOperandException 
     * @throws InvalidTypeException  
     * @throws InvalidInputException if {@code  operator == null || args == null || historyPoint == null}.
     */
    public abstract Primitive applyFunctionPrimitive(char type, HistoryPoint historyPoint, String operator, Value... args) 
    throws InvalidOperandException, InvalidTypeException, InvalidInputException;

    /**
     * Applies a unary {@link Operator} to a {@link Primitive}.
     * 
     * @param operator an {@link Operator}. It must be unary.
     * @param operand a {@link Primitive}.
     * @return a {@link Primitive} representing the application of {@code operator} 
     *         to {@code operand}.
     * @throws InvalidOperatorException when {@code operator} is not unary.
     * @throws InvalidOperandException when {@code operand} is {@code null}.
     * @throws InvalidTypeException when {@code operand} is not type compatible with
     *         the application of {@code operator}.
     */
    public Primitive applyUnary(Operator operator, Primitive operand) 
    throws InvalidOperatorException, InvalidOperandException, InvalidTypeException {
        final Primitive retVal;

        switch (operator) {
        case NEG:
            retVal = neg(operand);
            break;
        case NOT:
            retVal = not(operand);
            break;
        default:
            throw new InvalidOperatorException(operator.toString() + " is not unary");
        }    	
        return retVal;
    }

    /**
     * Applies a binary {@link Operator} to a {@link Primitive}.
     * 
     * @param firstOperand a {@link Primitive}.
     * @param operator an {@link Operator}. It must be binary.
     * @param secondOperand a {@link Primitive}.
     * @return a {@link Primitive} representing the application of {@code operator} to {@code firstOperand}
     *          and {@code secondOperand}.
     * @throws InvalidOperatorException  when {@code operator == null} or is not binary.
     * @throws InvalidOperandException when {@code firstOperand == null ||  secondOperand == null}. 
     * @throws InvalidTypeException when {@code firstOperand} and {@code secondOperand} 
     *         are not type compatible with the application of {@code operator}. 
     */
    public Primitive applyBinary(Primitive firstOperand, Operator operator, Primitive secondOperand) 
    throws InvalidOperatorException, InvalidOperandException, InvalidTypeException {
        final Primitive retVal;

        switch (operator) {
        case ADD:
            retVal = add(firstOperand, secondOperand);
            break;
        case SUB:
            retVal = sub(firstOperand, secondOperand);
            break;
        case MUL:
            retVal = mul(firstOperand, secondOperand);
            break;
        case DIV:
            retVal = div(firstOperand, secondOperand);
            break;
        case REM:
            retVal = rem(firstOperand, secondOperand);
            break;
        case SHL:
            retVal = shl(firstOperand, secondOperand);
            break;
        case SHR:
            retVal = shr(firstOperand, secondOperand);
            break;
        case USHR:
            retVal = ushr(firstOperand, secondOperand);
            break;
        case ORBW:
            retVal = orBitwise(firstOperand, secondOperand);
            break;
        case ANDBW:
            retVal = andBitwise(firstOperand, secondOperand);
            break;
        case XORBW:
            retVal = xorBitwise(firstOperand, secondOperand);
            break;
        case GE:
            retVal = ge(firstOperand, secondOperand);
            break;
        case AND:
            retVal = and(firstOperand, secondOperand);
            break;
        case OR:
            retVal = or(firstOperand, secondOperand);
            break;
        case NE:
            retVal = ne(firstOperand, secondOperand);
            break;
        case EQ:
            retVal = eq(firstOperand, secondOperand);
            break;
        case LE:
            retVal = le(firstOperand, secondOperand);
            break;
        case GT:
            retVal = gt(firstOperand, secondOperand);
            break;
        case LT:
            retVal = lt(firstOperand, secondOperand);
            break;
        default:
            throw new InvalidOperatorException(operator.toString() + " is not binary");
        }

        return retVal;
    }    

    /**
     * Converts a primitive value to another type.
     * 
     * @param type a {@code char} representing the type of the conversion.
     * @param arg a {@link Primitive}.
     * @return a {@link Primitive} representing the result of converting 
     *         {@code arg} to {@code type}, or {@code arg} if it already
     *         has type {@code type}.
     * @throws InvalidOperandException when {@code arg == null}. 
     * @throws InvalidTypeException when {@code arg} cannot be converted 
     *         to {@code type}. 
     */
    public Primitive to(char type, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException {
        if (arg == null) {
            throw new InvalidOperandException("Parameter arg of type conversion is null.");
        }
        final char argType = arg.getType();
        if (type == argType) {
            return arg;
        }
        if (widens(type, argType)) {
            return widen(type, arg);
        }
        if (narrows(type, argType)) {
            return narrow(type, arg);
        }
        throw new InvalidTypeException("cannot convert type " + argType + " to type " + type);
    }
    
    /**
     * Simplifies a {@link Primitive} to another equivalent {@link Primitive}.
     * 
     * @param arg a {@link Primitive}.
     * @return a {@link Primitive} equivalent to {@code arg}.
     *         The default implementation returns {@code arg}.
     */
    public Primitive simplify(Primitive arg) {
    	return arg;
    }
}