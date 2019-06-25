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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.NoSuchElementException;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

public abstract class Calculator {
    /** The (only) {@link Any} value. */
    private static final Any ANY;

    /** The boolean {@code true} value. */
    private static final Simplex TRUE; 

    /** The boolean {@code false} value. */
    private static final Simplex FALSE;

    /** The byte 0 value. */
    private static final Simplex ZERO_BYTE;

    /** The int 0 value. */
    private static final Simplex ZERO_INT;

    /** The long 0 value. */
    private static final Simplex ZERO_LONG;

    /** The short 0 value. */
    private static final Simplex ZERO_SHORT;

    /** The float 0 value. */
    private static final Simplex ZERO_FLOAT;

    /** The double 0 value. */
    private static final Simplex ZERO_DOUBLE;

    /** Default value for primitive type {@code boolean}. */
    private static final Simplex DEFAULT_BOOL;

    /** Default value for primitive type {@code byte}. */
    private static final Simplex DEFAULT_BYTE;

    /** Default value for primitive type {@code int}. */
    private static final Simplex DEFAULT_INT;

    /** Default value for primitive type {@code long}. */
    private static final Simplex DEFAULT_LONG;

    /** Default value for primitive type {@code short}. */
    private static final Simplex DEFAULT_SHORT;

    /** Default value for primitive type {@code float}. */
    private static final Simplex DEFAULT_FLOAT;

    /** Default value for primitive type {@code double}. */
    private static final Simplex DEFAULT_DOUBLE;

    /** Default value for primitive type {@code char}. */
    private static final Simplex DEFAULT_CHAR;

    /** Default value for reference types. */
    private static final ReferenceConcrete DEFAULT_REFERENCE;

    static {
        try {
            ANY               = Any.make();
            TRUE              = Simplex.make(Boolean.valueOf(true));
            FALSE             = Simplex.make(Boolean.valueOf(false));
            ZERO_BYTE         = Simplex.make(Byte.valueOf((byte) 0));
            ZERO_INT          = Simplex.make(Integer.valueOf(0));
            ZERO_LONG         = Simplex.make(Long.valueOf(0L));
            ZERO_SHORT        = Simplex.make(Short.valueOf((short) 0));
            ZERO_FLOAT        = Simplex.make(Float.valueOf(0.0f));
            ZERO_DOUBLE       = Simplex.make(Double.valueOf(0.0d));
            DEFAULT_BOOL      = FALSE;
            DEFAULT_BYTE      = ZERO_BYTE;
            DEFAULT_INT       = ZERO_INT;
            DEFAULT_LONG      = ZERO_LONG;
            DEFAULT_SHORT     = ZERO_SHORT;
            DEFAULT_FLOAT     = ZERO_FLOAT;
            DEFAULT_DOUBLE    = ZERO_DOUBLE;
            DEFAULT_CHAR      = Simplex.make(Character.valueOf('\u0000'));
            DEFAULT_REFERENCE = Null.getInstance();
        } catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
	/** The stack. */
    private final ArrayDeque<Primitive> stack = new ArrayDeque<>();

    /**
     * Factory method for values with type {@link Any}.
     * 
     * @return an {@link Any}.
     */
    public final Any valAny() {
        return ANY;
    }

    /**
     * Factory method for concrete values with type boolean. 
     * 
     * @param value a {@code boolean}.
     * @return a {@link Simplex} representing {@code value}.
     */
    public final Simplex valBoolean(boolean value) {
        return (value ? TRUE : FALSE);
    }

    /**
     * Factory method for concrete values with type boolean. 
     * 
     * @param value a {@code boolean}.
     * @return a {@link Simplex} representing {@code value}. 
     */
    public final Simplex valByte(byte value) {
        if (value == ((byte) 0)) {
            return ZERO_BYTE;
        }
        try {
            return Simplex.make(Byte.valueOf(value));
        } catch (InvalidOperandException e) {
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
    public final Simplex valShort(short value) {
        if (value == ((short) 0)) {
            return ZERO_SHORT;
        }
        try {
            return Simplex.make(Short.valueOf(value));
        } catch (InvalidOperandException e) {
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
    public final Simplex valInt(int value) {
        if (value == 0) {
            return ZERO_INT;
        }
        try {
            return Simplex.make(Integer.valueOf(value));
        } catch (InvalidOperandException e) {
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
    public final Simplex valLong(long value) {
        if (value == 0L) {
            return ZERO_LONG;
        }
        try {
            return Simplex.make(Long.valueOf(value));
        } catch (InvalidOperandException e) {
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
    public final Simplex valFloat(float value) {
        if (value == 0.0f) {
            return ZERO_FLOAT;
        }
        try {
            return Simplex.make(Float.valueOf(value));
        } catch (InvalidOperandException e) {
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
    public final Simplex valDouble(double value) {
        if (value == 0.0d) {
            return ZERO_DOUBLE;
        }
        try {
            return Simplex.make(Double.valueOf(value));
        } catch (InvalidOperandException e) {
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
    public final Simplex valChar(char value) {
        try {
            return Simplex.make(Character.valueOf(value));
        } catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Factory method for {@link Term}s.
     * 
     * @param type a {@code char} representing the type of the symbol 
     *        (see {@link Type}).
     * @param value a {@link String} representing the conventional
     *        value of the term.
     * @return a {@link Term}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    public final Term valTerm(char type, String value) throws InvalidTypeException {
    	return new Term(type, value);
    }

     //same methods overloaded

    public Simplex val_(double val) {
        return valDouble(val);
    }

    public Simplex val_(float val) {
        return valFloat(val);
    }

    public Simplex val_(long val) {
        return valLong(val);
    }

    public Simplex val_(int val) {
        return valInt(val);
    }

    public Simplex val_(short val) {
        return valShort(val);
    }

    public Simplex val_(byte val) {
        return valByte(val);
    }

    public Simplex val_(char val) {
        return valChar(val);
    }

    public Simplex val_(boolean val) {
        return valBoolean(val);
    }

    public Simplex val_(Object v) throws InvalidInputException {
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
            throw new InvalidInputException("Tried to convert to a Value an object that does not box a primitive type; object's class: " + v.getClass().getName());
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
    public Value valDefault(char type) {
        switch (type) {
        case BYTE:
            return DEFAULT_BYTE;
        case SHORT:
            return DEFAULT_SHORT;
        case INT:
            return DEFAULT_INT;
        case LONG:
            return DEFAULT_LONG;
        case FLOAT:
            return DEFAULT_FLOAT;
        case DOUBLE:
            return DEFAULT_DOUBLE;
        case CHAR:
            return DEFAULT_CHAR;
        case BOOLEAN:
            return DEFAULT_BOOL;
        case ARRAYOF:
        case NULLREF:
        case REFERENCE:
            return DEFAULT_REFERENCE;
        default: //Type.VOID
            return null;
        }
    }
    
    /**
     * Pushes an operand on the calculator's stack.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     */
    public final Calculator push(Primitive operand) throws InvalidOperandException {
    	if (operand == null) {
    		throw new InvalidOperandException("Tried to push a null operand.");
    	}
    	this.stack.push(operand);
    	return this;
    }
    
    /**
     * Pops the topmost operand in this calculator's stack.
     * 
     * @return a {@link Primitive} if the stack is not empty.
     * @throws NoSuchElementException if the stack is empty.
     */
    public final Primitive pop() throws NoSuchElementException {
    	return this.stack.pop();
    }
    
    /**
     * Pushes on this calculator's stack the {@link Any} value.
     * 
     * @return this {@link Calculator}.
     */
    public Calculator pushAny() {
    	try {
    		return push(valAny());
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }
    
    /**
     * Pushes on this calculator's stack a value with type
     * boolean.
     * 
     * @param value a {@code boolean}.
     * @return this {@link Calculator}.
     */
    public Calculator pushBoolean(boolean value) {
    	try {
    		return push(valBoolean(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }
    
    /**
     * Pushes on this calculator's stack a value with type
     * byte.
     * 
     * @param value a {@code byte}.
     * @return this {@link Calculator}.
     */
    public Calculator pushByte(byte value) {
    	try {
    		return push(valByte(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * short.
     * 
     * @param value a {@code short}.
     * @return this {@link Calculator}.
     */
    public Calculator pushShort(short value) {
    	try {
    		return push(valShort(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * int.
     * 
     * @param value an {@code int}.
     * @return this {@link Calculator}.
     */
    public Calculator pushInt(int value) {
    	try {
    		return push(valInt(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * long.
     * 
     * @param value a {@code long}.
     * @return this {@link Calculator}.
     */
    public Calculator pushLong(long value) {
    	try {
    		return push(valLong(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * float.
     * 
     * @param value a {@code float}.
     * @return this {@link Calculator}.
     */
    public Calculator pushFloat(float value) {
    	try {
    		return push(valFloat(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * double.
     * 
     * @param value a {@code double}.
     * @return this {@link Calculator}.
     */
    public Calculator pushDouble(double value) {
    	try {
    		return push(valDouble(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * char.
     * 
     * @param value a {@code char}.
     * @return this {@link Calculator}.
     */
    public Calculator pushChar(char value) {
    	try {
    		return push(valChar(value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Pushes on this calculator's stack a value with type
     * {@link Term}.
     * 
     * @param type a {@code char} representing the type of the symbol 
     *        (see {@link Type}).
     * @param value a {@link String} representing the conventional
     *        value of the term.
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    public Calculator pushTerm(char type, String value) throws InvalidTypeException {
    	try {
    		return push(valTerm(type, value));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    //same methods overloaded

    public Calculator pushVal(double value) {
    	return pushDouble(value);
    }

    public Calculator pushVal(float value) {
    	return pushFloat(value);
    }

    public Calculator pushVal(long value) {
    	return pushLong(value);
    }

    public Calculator pushVal(int value) {
    	return pushInt(value);
    }

    public Calculator pushVal(short value) {
    	return pushShort(value);
    }

    public Calculator pushVal(byte value) {
    	return pushByte(value);
    }

    public Calculator pushVal(char value) {
    	return pushChar(value);
    }

    public Calculator pushVal(boolean value) {
    	return pushBoolean(value);
    }

    public Calculator pushVal(Object v) throws InvalidInputException {
        if (v instanceof Byte) {
            return pushByte(((Byte) v).byteValue());
        } else if (v instanceof Short) {
            return pushShort(((Short) v).shortValue());
        } else if (v instanceof Integer) {
            return pushInt(((Integer) v).intValue());
        } else if (v instanceof Long) {
            return pushLong(((Long) v).longValue());
        } else if (v instanceof Float) {
            return pushFloat(((Float) v).floatValue());
        } else if (v instanceof Double) {
            return pushDouble(((Double) v).doubleValue());
        } else if (v instanceof Boolean) {
            return pushBoolean(((Boolean) v).booleanValue());
        } else if (v instanceof Character) {
            return pushChar(((Character) v).charValue());
        } else {
            throw new InvalidInputException("Tried to push an object that does not box a primitive type; object's class: " + v.getClass().getName());
        }
    }

    /**
     * Applies a functional operator returning a {@link Primitive} to a list of arguments
     * and immediately returns the result.
     *  
     * @param type a {@code char} representing the type of the return value of {@code operator}
     *        (see {@link Type}).
     * @param historyPoint the current {@link HistoryPoint}.
     * @param operator a {@code String} representing the function to be applied.
     * @param args a varargs of {@link Value}s representing the arguments to the function.
     * @return a {@link Primitive}.
     * @throws InvalidTypeException if {@code type} is not primitive.  
     * @throws InvalidInputException if {@code  historyPoint == null || operator == null || args == null}
     *         or any of {@code args[i]} is {@code null}.
     */
    public final Primitive applyFunctionPrimitiveAndPop(char type, HistoryPoint historyPoint, String operator, Value... args) 
    throws InvalidTypeException, InvalidInputException {
    	return simplify(new PrimitiveSymbolicApply(type, historyPoint, operator, args));
    }
    
    /**
     * Applies a function returning a {@link Primitive} to a list of arguments and
     * pushes the result on the top of the stack.
     *  
     * @param type a {@code char} representing the type of the return value of {@code operator}
     *        (see {@link Type}).
     * @param historyPoint the current {@link HistoryPoint}.
     * @param operator a {@code String} representing the function to be applied.
     * @param args a varargs of {@link Value}s representing the arguments to the function.
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if {@code type} is not primitive.  
     * @throws InvalidInputException if {@code  historyPoint == null || operator == null || args == null}
     *         or any of {@code args[i]} is {@code null}.
     */
    public final Calculator applyFunctionPrimitive(char type, HistoryPoint historyPoint, String operator, Value... args) 
    throws InvalidTypeException, InvalidInputException {
        try {
			return push(applyFunctionPrimitiveAndPop(type, historyPoint, operator, args));
		} catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }
   
    /**
     * Calculates the arithmetic sum of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the addend.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator add(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.ADD, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic multiplication of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the multiplier.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator mul(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.MUL, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic subtraction of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the subtrahend.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator sub(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.SUB, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic division of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the divisor.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator div(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.DIV, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic remainder of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the divisor.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator rem(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.REM, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic negation of the topmost 
     * {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if the operand has incompatible type.
     */
    public final Calculator neg() 
    throws InvalidTypeException {
    	try {
			return applyUnary(Operator.NEG);
		} catch (InvalidOperatorException e) {
    		//this should never happen
    		throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the bitwise AND of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator andBitwise(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.ANDBW, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the bitwise OR of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator orBitwise(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.ORBW, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the bitwise XOR of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator xorBitwise(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.XORBW, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the logical AND of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator and(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.AND, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the logical OR of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator or(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.OR, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the logical NOT of the topmost 
     * {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if the operand has incompatible type.
     */
    public final Calculator not() throws InvalidTypeException {
    	try {
			return applyUnary(Operator.NOT);
		} catch (InvalidOperatorException e) {
    		//this should never happen
    		throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the left shift of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the shift amount.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator shl(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.SHL, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic right shift of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the shift amount.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator shr(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.SHR, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the logical right shift of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}, the shift amount.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator ushr(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.USHR, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the equality comparison of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator eq(Primitive operand) 
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.EQ, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the inequality comparison of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator ne(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.NE, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic less-or-equal-than comparison of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator le(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.LE, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic less-than comparison of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator lt(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.LT, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic greater-or-equal-than comparison of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator ge(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.GE, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Calculates the arithmetic greater-than comparison of a {@link Primitive}
     * with the topmost {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator gt(Primitive operand)
    throws InvalidOperandException, InvalidTypeException {
    	try {
			return applyBinary(Operator.GT, operand);
		} catch (InvalidOperatorException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Applies a unary {@link Operator} to the topmost 
     * {@link Primitive} on the stack, and replaces
     * the top of the stack with the result.
     * 
     * @param operator an {@link Operator}. It must be unary.
     * @return this {@link Calculator}.
     * @throws InvalidOperatorException if {@code operator == null} or 
     *         {@code operator} is not unary.
     * @throws InvalidTypeException if the operand has incompatible type.
     */
    public final Calculator applyUnary(Operator operator) 
    throws InvalidOperatorException, InvalidTypeException {
    	if (operator == null || operator.isBinary()) {
            throw new InvalidOperatorException("Tried to apply operator " + operator + " to build a unary expression.");
    	} else {
        	final Primitive operand = pop();
            try {
                push(simplify(Expression.makeExpressionUnary(operator, operand)));
            } catch (InvalidOperatorException | InvalidOperandException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
            return this;
    	}
    }

    /**
     * Applies a binary {@link Operator} to a {@link Primitive}.
     * 
     * @param operator an {@link Operator}. It must be binary.
     * @param operand a {@link Primitive}.
     * @return this {@link Calculator}.
     * @throws InvalidOperatorException if {@code operator == null} or 
     *         {@code operator} is not binary.
     * @throws InvalidOperandException if {@code operand == null}. 
     * @throws InvalidTypeException if the operands have incompatible types.
     */
    public final Calculator applyBinary(Operator operator, Primitive operand) 
    throws InvalidOperatorException, InvalidOperandException, InvalidTypeException {
    	if (operator == null || !operator.isBinary()) {
            throw new InvalidOperatorException("Tried to apply operator " + operator + " to build a binary expression.");
    	} else if (operand == null) {
        	throw new InvalidOperandException("Tried to build a binary expression with a null operand.");
    	} else {
        	final Primitive firstOperand = pop();
        	try {
        		push(simplify(Expression.makeExpressionBinary(firstOperand, operator, operand)));
        	} catch (InvalidOperatorException | InvalidOperandException e) {
        		//this should never happen
        		throw new UnexpectedInternalException(e);
        	}
        	return this;
    	}
    }    

    /**
     * Converts the topmost {@link Primitive} on the stack to a 
     * wider type, and replaces the top of the stack with the result.
     * 
     * @param type a {@code char} representing the type of the conversion (see {@link Type}).
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if the operand cannot be widened to {@code type},
     *         or {@code type} is not a valid primitive type.
     */
    public final Calculator widen(char type) throws InvalidTypeException {
    	final Primitive operand = pop();
        try {
            push(simplify(WideningConversion.make(type, operand)));
        } catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        return this;
    }

    /**
     * Converts the topmost {@link Primitive} on the stack to a 
     * narrower type, and replaces the top of the stack with the result.
     * 
     * @param type a {@code char} representing the type of the conversion (see {@link Type}).
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if the operand cannot be narrowed to {@code type},
     *         or {@code type} is not a valid primitive type.
     */
    public final Calculator narrow(char type) throws InvalidTypeException {
    	final Primitive operand = pop();
        try {
            push(simplify(NarrowingConversion.make(type, operand)));
        } catch (InvalidOperandException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        return this;
    }

    /**
     * Converts the topmost {@link Primitive} on the stack to another type, 
     * and replaces the top of the stack with the result.
     * 
     * @param type a {@code char} representing the type of the conversion.
     * @return this {@link Calculator}.
     * @throws InvalidTypeException if the operand cannot be converted to {@code type},
     *         or {@code type} is not a valid primitive type.
     */
    public final Calculator to(char type) throws InvalidTypeException {
    	final Primitive operand = pop();
        final char operandType = operand.getType();
        if (type == operandType) {
            try {
				push(operand);
			} catch (InvalidOperandException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
			}
        } else if (widens(type, operandType)) {
            try {
                push(simplify(WideningConversion.make(type, operand)));
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        } else if (narrows(type, operandType)) {
            try {
                push(simplify(NarrowingConversion.make(type, operand)));
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        } else {
        	throw new InvalidTypeException("Cannot convert operand with type " + operandType + " to type " + type + ".");
        }
        return this;
    }
    
    private static final class ReplacementTriple {
    	private final Primitive operand;
    	private final Primitive from;
    	private final Primitive to;
    	private final int hashCode;
    	
    	public ReplacementTriple(Primitive operand, Primitive from, Primitive to) {
    		this.operand = operand;
    		this.from = from;
    		this.to = to;
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : System.identityHashCode(from));
			result = prime * result + ((operand == null) ? 0 : System.identityHashCode(operand));
			result = prime * result + ((to == null) ? 0 : System.identityHashCode(to));
			this.hashCode = result;
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
			final ReplacementTriple other = (ReplacementTriple) obj;
			if (this.from == null) {
				if (other.from != null) {
					return false;
				}
			} else if (this.from != other.from) {
				return false;
			}
			if (this.operand == null) {
				if (other.operand != null) {
					return false;
				}
			} else if (this.operand != other.operand) {
				return false;
			}
			if (this.to == null) {
				if (other.to != null) {
					return false;
				}
			} else if (this.to != other.to) {
				return false;
			}
			return true;
		}
    	
    	
    }
    
    private final HashMap<ReplacementTriple, Primitive> replaceCache = new HashMap<>();
    
    private class PrimitiveReplaceVisitor implements PrimitiveVisitor {
    	final Primitive from, to;
    	Primitive result;
    	
    	public PrimitiveReplaceVisitor(Primitive from, Primitive to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public void visitAny(Any x) {
			this.result = x; //nothing to do
		}

		@Override
		public void visitExpression(Expression expression) throws Exception {
	    	//trivial case: the result is in the cache
	    	final ReplacementTriple key = new ReplacementTriple(expression, this.from, this.to);
	    	if (Calculator.this.replaceCache.containsKey(key)) {
	    		this.result = Calculator.this.replaceCache.get(key);
	    		return;
	    	}
	    	
	        final Primitive first;
	        if (expression.isUnary()) {
	            first = null;
	        } else if (expression.getFirstOperand().equals(this.from)) {
	            first = this.to;
	        } else {
	            expression.getFirstOperand().accept(this);
	            first = this.result;
	        }

	        final Primitive second;
	        if (expression.getSecondOperand().equals(this.from)) {
	            second = this.to;
	        } else {
	            expression.getSecondOperand().accept(this);
	            second = this.result;
	        }

	        if (expression.isUnary()) {
	        	this.result = simplify(Expression.makeExpressionUnary(expression.getOperator(), second));
	        } else {
	        	this.result = simplify(Expression.makeExpressionBinary(first, expression.getOperator(), second));
	        }
	        Calculator.this.replaceCache.put(key, this.result);
		}

		@Override
		public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
	    	//trivial case: the result is in the cache
	    	final ReplacementTriple key = new ReplacementTriple(x, this.from, this.to);
	    	if (Calculator.this.replaceCache.containsKey(key)) {
	    		this.result = Calculator.this.replaceCache.get(key);
	    		return;
	    	}
	    	
		    final Value[] args = x.getArgs();
		    final Value[] argsNew = new Value[args.length];
		    for (int i = 0; i < args.length; ++i) {
		        if (args[i].equals(this.from)) {
		            argsNew[i] = this.to;
		        } else if (args[i] instanceof Primitive) {
		        	((Primitive) args[i]).accept(this);
		        	argsNew[i] = this.result;
		        } else {
		            argsNew[i] = args[i];
		        }
		    }
		    
		    this.result = simplify(new PrimitiveSymbolicApply(x.getType(), x.historyPoint(), x.getOperator(), argsNew));
	        Calculator.this.replaceCache.put(key, this.result);
		}

		@Override
		public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) {
			this.result = s; //nothing to do
		}

		@Override
		public void visitSimplex(Simplex x) throws Exception {
			this.result = x; //nothing to do
		}

		@Override
		public void visitTerm(Term x) throws Exception {
			this.result = x; //nothing to do
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
	    	//trivial case: the result is in the cache
	    	final ReplacementTriple key = new ReplacementTriple(x, this.from, this.to);
	    	if (Calculator.this.replaceCache.containsKey(key)) {
	    		this.result = Calculator.this.replaceCache.get(key);
	    		return;
	    	}
	    	
	        final Primitive arg;
	        if (x.getArg().equals(this.from)) {
	        	arg = this.to;
	        } else {
	        	x.getArg().accept(this);
	        	arg = this.result;
	        }
	        this.result = simplify(NarrowingConversion.make(x.getType(), arg));
	        Calculator.this.replaceCache.put(key, this.result);
		}

		@Override
		public void visitWideningConversion(WideningConversion x) throws Exception {
	    	//trivial case: the result is in the cache
	    	final ReplacementTriple key = new ReplacementTriple(x, this.from, this.to);
	    	if (Calculator.this.replaceCache.containsKey(key)) {
	    		this.result = Calculator.this.replaceCache.get(key);
	    		return;
	    	}
	    	
	        final Primitive arg;
	        if (x.getArg().equals(this.from)) {
	        	arg = this.to;
	        } else {
	        	x.getArg().accept(this);
	        	arg = this.result;
	        }
	        this.result = simplify(WideningConversion.make(x.getType(), arg));
	        Calculator.this.replaceCache.put(key, this.result);
		}
    }
    
    /**
     * Replaces all the occurrences of a {@link Primitive} in the
     * topmost {@link Primitive} of the stack with another {@link Primitive}, 
     * and replaces the top of the stack with the result.
     * 
     * @param from the {@link Primitive} to be replaced. It must not be {@code null}.
     * @param to the {@link Primitive} to replace with. It must not be {@code null}.
     * @return this {@link Calculator}.
     * @throws InvalidInputException if {@code from == null || to == null}.
     * @throws InvalidTypeException if {@code from} and {@code to} have
     *         different type.
     */
    public Calculator replace(Primitive from, Primitive to) 
    throws InvalidInputException, InvalidTypeException {
    	if (from == null || to == null) {
    		throw new InvalidInputException("Attempted to invoke " + getClass().getName() + ".replace with null Primitive from or Primitive to parameter.");
    	}
    	if (from.getType() != to.getType()) {
    		throw new InvalidTypeException("Attempted to invoke " + getClass().getName() + ".replace with Primitive from and Primitive to parameters having different type.");
    	}
    	
    	//trivial case: from and to do not differ
        if (from.equals(to)) {
            return this;
        }
        
    	final Primitive operand = pop();
    	
    	//trivial case: the operand is from (the result is to)
    	if (operand.equals(from)) {
    		try {
				push(to);
				return this;
			} catch (InvalidOperandException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
    	}
    	
    	//nontrivial case: the operand is not from, and from and to differ 
    	//(applies the visitor)
    	try {
    		final PrimitiveReplaceVisitor v = new PrimitiveReplaceVisitor(from, to);
    		operand.accept(v);
    		push(v.result);
    		return this;
		} catch (Exception e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }
    
    /**
     * Simplifies a {@link Primitive} to another equivalent 
     * {@link Primitive}.
     * 
     * @param arg a {@link Primitive}.
     * @return a {@link Primitive} equivalent to {@code arg}.
     *         The default implementation returns {@code arg}.
     */
    public Primitive simplify(Primitive arg) {
    	return arg;
    }
}