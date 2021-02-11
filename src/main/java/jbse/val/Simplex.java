package jbse.val;

import static jbse.val.Util.asCharacterLiteral;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class for concrete primitive values.
 */
public final class Simplex extends Primitive implements Cloneable {	
    /** The primitive value this object represents. */
    private final Object value;

    /** The hash code. */
    private final int hashCode;

    /** The string representation of this object. */
    private final String toString;

    /**
     * Constructor.
     * 
     * @param type a {@code char}, the type of this value.
     * @param value a (boxed) value with primitive type.
     * @throws InvalidOperandException if {@code value} is not a 
     *         primitive value (i.e., an instance of {@link Boolean}, {@link Byte}, 
     *         {@link Character}, {@link Double}, {@link Float}, {@link Integer}, 
     *         {@link Long}, or {@link Short}).
     * @throws InvalidTypeException if {@code type} is not primitive or is
     *         not the type of {@code value}.
     */
    private Simplex(char type, Object value) 
    throws InvalidOperandException, InvalidTypeException {
        super(type);
        //checks on parameters
        if (value == null || !(
            value instanceof Boolean || 
            value instanceof Byte || 
            value instanceof Character ||
            value instanceof Double ||
            value instanceof Float || 
            value instanceof Integer ||
            value instanceof Long ||
            value instanceof Short)) {
            throw new InvalidOperandException("no operand in simplex construction");
        }
        if ((type == Type.BOOLEAN && !(value instanceof Boolean)) ||
            (type == Type.BYTE && !(value instanceof Byte)) ||
            (type == Type.CHAR && !(value instanceof Character)) ||
            (type == Type.DOUBLE && !(value instanceof Double)) ||
            (type == Type.FLOAT && !(value instanceof Float)) ||
            (type == Type.INT && !(value instanceof Integer)) ||
            (type == Type.LONG && !(value instanceof Long)) ||
            (type == Type.SHORT && !(value instanceof Short))) {
            throw new InvalidTypeException("type does not agree with value in simplex construction");
        }
        this.value = value;

        //calculates hashCode
        final int prime = 31;
        int result = 1;
        result = prime + result * this.value.hashCode();
        this.hashCode = result;

        //calculates toString
        if (type == Type.BOOLEAN) {
        	this.toString = this.value.toString();
        } else if (type == Type.BYTE) {
        	this.toString = "(byte) " + this.value.toString();
        } else if (type == Type.CHAR) {
        	this.toString = asCharacterLiteral(((Character) this.value).charValue());
        } else if (type == Type.DOUBLE) {
        	this.toString = this.value.toString() + "d";
        } else if (type == Type.FLOAT) {
        	this.toString = this.value.toString() + "f";
        } else if (type == Type.INT) {
        	this.toString = this.value.toString();
        } else if (type == Type.LONG) {
        	this.toString = this.value.toString() + "L";
        } else { //type == Type.SHORT
            this.toString = "(short) " + this.value.toString();
        }
    }
    
    /**
     * Factory method for {@link Simplex} values.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param value a (boxed) value with primitive type. 
     * @throws InvalidOperandException if {@code value} is not a boxed
     *         primitive value (i.e., an instance of {@link Boolean}, {@link Byte}, 
     *         {@link Character}, {@link Double}, {@link Float}, {@link Integer}, 
     *         {@link Long}, or {@link Short}).
     */
    public static Simplex make(Object n) throws InvalidOperandException {
        try {
        	if (n instanceof Boolean) {
        		return new Simplex(Type.BOOLEAN, n);
        	} else if (n instanceof Byte) {
        		return new Simplex(Type.BYTE, n);
        	} else if (n instanceof Character) {
        		return new Simplex(Type.CHAR, n);
        	} else if (n instanceof Double) {
        		return new Simplex(Type.DOUBLE, n);
        	} else if (n instanceof Float) {
        		return new Simplex(Type.FLOAT, n);
        	} else if (n instanceof Integer) {
        		return new Simplex(Type.INT, n);
        	} else if (n instanceof Long) {
        		return new Simplex(Type.LONG, n);
        	} else {
        		return new Simplex(Type.SHORT, n);
        	}
		} catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

    /**
     * Returns the (Java) value of this {@link Simplex} value.
     * 
     * @return the value as {@link Object}, either {@link Boolean},
     *         {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     *         {@link Float}, {@link Double}, or {@link Character}. 
     */
    public Object getActualValue() {
        return this.value;
    }

    /**
     * Checks whether this object represents the value zero 
     * or the value one.
     * 
     * @param zero {@code true} iff this object must be checked
     *        against zero, {@code false} iff it must be checked
     *        against one.
     * @return {@code true} iff this object is equal to zero or one, 
     *         according to the value of the parameter {@code zero}, 
     *         for its type. If this object is a boolean, it will be
     *         compared against {@code false} when {@code zero == true}
     *         and against {@code true} when {@code zero == false}.
     */
    public boolean isZeroOne(boolean zero) {
        byte b;
        short s;
        char c;
        int i;
        long l;
        float f;
        double d;
        boolean z;

        if (zero) {
            b = 0;
            s = 0;
            c = 0;
            i = 0;
            l = 0L;
            f = 0F; //TODO negative zero
            d = 0D; //TODO negative zero
            z = false;
        } else {
            b = 1;
            s = 1;
            c = 1;
            i = 1;
            l = 1L;
            f = 1F;
            d = 1D;
            z = true;
        }

        boolean retVal = false;
        if (getType() == Type.BYTE) {
            retVal = (((Byte) getActualValue()).byteValue() == b);
        } else if (getType() == Type.SHORT) {
            retVal = (((Short) getActualValue()).shortValue() == s);
        } else if (getType() == Type.CHAR) {
            retVal = (((Character) getActualValue()).charValue() == c);
        } else if (getType() == Type.INT) {
            retVal = (((Integer) getActualValue()).intValue() == i);
        } else if (getType() == Type.LONG) {
            retVal = (((Long) getActualValue()).longValue() == l);
        } else if (getType() == Type.FLOAT) {
            retVal = (((Float) getActualValue()).floatValue() == f);
        } else if (getType() == Type.DOUBLE) {
            retVal = (((Double) getActualValue()).doubleValue() == d);
        } else if (getType() == Type.BOOLEAN) {
            retVal = (((Boolean) getActualValue()).booleanValue() == z);
        } else {
        	//this should never happen
        	throw new UnexpectedInternalException("Found a Simplex object with type " + getType() + ".");
        }

        return retVal;
    }

    /**
     * {@inheritDoc}
     * For {@link Simplex} it returns {@code true} iff
     * the object has type boolean and represents the {@code true} value.
     * In all other cases it returns {@code false}.
     */
    @Override
    public boolean surelyTrue() {
        return (this.getType() == Type.BOOLEAN && ((Boolean) this.value).booleanValue());
    }

    /**
     * {@inheritDoc}
     * For {@link Simplex} it returns {@code true} iff
     * the object has type boolean and represents the {@code false} value.
     * In all other cases it returns {@code false}.
     */
    @Override
    public boolean surelyFalse() {
        return (this.getType() == Type.BOOLEAN && !((Boolean) this.value).booleanValue());
    }

    @Override
    public void accept(PrimitiveVisitor v) throws Exception {
        v.visitSimplex(this);
    }

    /**
     * {@inheritDoc}
     * For {@link Simplex} instances it always returns {@code false}.
     */
    @Override
    public boolean isSymbolic() {
        return false;
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
        Simplex other = (Simplex) obj;
        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else { 
            if (!this.value.equals(other.value)) {
                return false;
            }
        }
        return true;
    }
}