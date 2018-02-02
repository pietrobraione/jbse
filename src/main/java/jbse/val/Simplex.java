package jbse.val;

import jbse.common.Type;
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
    
    private Simplex(char type, Calculator calc, Object value) 
    throws InvalidOperandException, InvalidTypeException {
    	super(type, calc);
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
    	this.toString = this.value.toString();
    }
    
	/**
     * Factory method for {@link Simplex} values.
     * 
	 * @param calc a {@link Calculator}.
	 * @param value the value as {@link Object}, either {@link Boolean},
     *         {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     *         {@link Float}, {@link Double}, or {@link Character}. 
	 */
    public static Simplex make(Calculator calc, Object n) 
    throws InvalidTypeException, InvalidOperandException {
        if (n instanceof Boolean) {
        	return new Simplex(Type.BOOLEAN, calc, n);
        } else if (n instanceof Byte) {
        	return new Simplex(Type.BYTE, calc, n);
        } else if (n instanceof Character) {
        	return new Simplex(Type.CHAR, calc, n);
        } else if (n instanceof Double) {
        	return new Simplex(Type.DOUBLE, calc, n);
        } else if (n instanceof Float) {
        	return new Simplex(Type.FLOAT, calc, n);
        } else if (n instanceof Integer) {
        	return new Simplex(Type.INT, calc, n);
        } else if (n instanceof Long) {
        	return new Simplex(Type.LONG, calc, n);
        } else {
        	return new Simplex(Type.SHORT, calc, n);
        }
    }
    
	/**
     * Returns the value of the simplex value.
     * 
     * @return the value as {@link Object}, either of {@link Boolean},
     *         {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     *         {@link Float}, {@link Double}, or {@link Character}. 
     */
    public Object getActualValue(){
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
     *         for its type.
     */
    public boolean isZeroOne(boolean zero) {
    	byte b;
    	char c;
    	int i;
    	long l;
    	float f;
    	double d;
    	
    	if (zero) {
    		b = 0;
    		c = 0;
    		i = 0;
    		l = 0L;
    		f = 0F; //TODO negative zero
    		d = 0D; //TODO negative zero
    	} else {
    		b = 1;
    		c = 1;
    		i = 1;
    		l = 1L;
    		f = 1F;
    		d = 1D;
    	}
    	
		boolean retVal = false;
		if (this.getType() == Type.BYTE) {
			retVal = (((Byte) this.getActualValue()) == b);
		} else if (this.getType() == Type.CHAR) {
			retVal = (((Character) this.getActualValue()) == c);
		} else if (this.getType() == Type.INT) {
			retVal = (((Integer) this.getActualValue()) == i);
		} else if (this.getType() == Type.LONG) {
			retVal = (((Long) this.getActualValue()) == l);
		} else if (this.getType() == Type.FLOAT) {
			retVal = (((Float) this.getActualValue()) == f);
		} else if (this.getType() == Type.DOUBLE) {
			retVal = (((Double) this.getActualValue()) == d);
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
	@Override
	public Object getValueForNative() {
		return this.value;
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