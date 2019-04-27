package jbse.val;

import static jbse.bc.Signatures.JAVA_STRICTMATH_ABS_DOUBLE;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ABS_FLOAT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ABS_INT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ABS_LONG;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ACOS;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ASIN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ATAN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_COS;
import static jbse.bc.Signatures.JAVA_STRICTMATH_EXP;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MAX_DOUBLE;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MAX_FLOAT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MAX_INT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MAX_LONG;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MIN_DOUBLE;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MIN_FLOAT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MIN_INT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_MIN_LONG;
import static jbse.bc.Signatures.JAVA_STRICTMATH_POW;
import static jbse.bc.Signatures.JAVA_STRICTMATH_SQRT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_SIN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_TAN;

import java.util.Arrays;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class representing the {@link PrimitiveSymbolicComputed} returned by the 
 * execution of a pure method on a set of {@link Value}s.
 * 
 * @author Pietro Braione
 */
public final class PrimitiveSymbolicApply extends PrimitiveSymbolicComputed implements SymbolicApply {
    //pure functions implemented in java.lang.StrictMath 
    
    /** Absolute value (double) */
    public static final String ABS_DOUBLE = JAVA_STRICTMATH_ABS_DOUBLE.toString();
    
    /** Absolute value (float) */
    public static final String ABS_FLOAT = JAVA_STRICTMATH_ABS_FLOAT.toString();
    
    /** Absolute value (int) */
    public static final String ABS_INT = JAVA_STRICTMATH_ABS_INT.toString();
    
    /** Absolute value (long) */
    public static final String ABS_LONG = JAVA_STRICTMATH_ABS_LONG.toString();
    
    /** Trigonometric sine */
    public static final String SIN = JAVA_STRICTMATH_SIN.toString();
    
    /** Trigonometric cosine */
    public static final String COS = JAVA_STRICTMATH_COS.toString();
    
    /** Trigonometric tangent */
    public static final String TAN = JAVA_STRICTMATH_TAN.toString();
    
    /** Trigonometric arc sine */
    public static final String ASIN = JAVA_STRICTMATH_ASIN.toString();
    
    /** Trigonometric arc cosine */
    public static final String ACOS = JAVA_STRICTMATH_ACOS.toString();
    
    /** Trigonometric arc tangent */
    public static final String ATAN = JAVA_STRICTMATH_ATAN.toString();
    
    /** Square root */
    public static final String SQRT = JAVA_STRICTMATH_SQRT.toString();
    
    /** Power */
    public static final String POW = JAVA_STRICTMATH_POW.toString();
    
    /** Exponential */
	public static final String EXP = JAVA_STRICTMATH_EXP.toString();
    
    /** Minimum (double) */
    public static final String MIN_DOUBLE = JAVA_STRICTMATH_MIN_DOUBLE.toString();
    
    /** Minimum (float) */
    public static final String MIN_FLOAT = JAVA_STRICTMATH_MIN_FLOAT.toString();
    
    /** Minimum (int) */
    public static final String MIN_INT = JAVA_STRICTMATH_MIN_INT.toString();
    
    /** Minimum (long) */
    public static final String MIN_LONG = JAVA_STRICTMATH_MIN_LONG.toString();
    
    /** Maximum */
    public static final String MAX = "max";
    
    /** Maximum (double) */
    public static final String MAX_DOUBLE = JAVA_STRICTMATH_MAX_DOUBLE.toString();
    
    /** Maximum (float) */
    public static final String MAX_FLOAT = JAVA_STRICTMATH_MAX_FLOAT.toString();
    
    /** Maximum (int) */
    public static final String MAX_INT = JAVA_STRICTMATH_MAX_INT.toString();
    
    /** Maximum (long) */
    public static final String MAX_LONG = JAVA_STRICTMATH_MAX_LONG.toString();
    
    /** The function name. */
	private final String operator;
	
	/** The args to which the function is applied, implicitly defining its arity. */
	private final Value[] args;
	
	/** The hash code of this object. */
    private final int hashCode;

    /** The string representation of this object. */
	private final String toString;
	
	private final String originString;
	
	/**
	 * Constructor. 
	 * 
     * @param type a {@code char}, the type of this {@link PrimitiveSymbolicApply}. 
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @param calc a {@link Calculator}.
     * @param operator the name of the function.
     * @param args the {@link Value} arguments to which the function is applied.
	 * @throws InvalidTypeException if {@code type} is not primitive.
	 * @throws InvalidInputException if {@code operator == null || args == null || historyPoint == null} 
	 *         or any of {@code args[i]} is {@code null}.
	 */
	public PrimitiveSymbolicApply(char type, HistoryPoint historyPoint, String operator, Value... args) 
	throws InvalidTypeException, InvalidInputException {
		super(type, historyPoint);
    	if (operator == null || args == null) {
            throw new InvalidInputException("Attempted to build a PrimitiveSymbolicApply with null operator or args.");
    	}
		this.operator = operator;
		this.args = args.clone();
		int i = 0;
		for (Value v : this.args) {
			if (v == null) {
				throw new InvalidInputException(i + (i == 1 ? "-st" : i == 2 ? "-nd" : i == 3 ? "-rd ": "-th") + " argument is null");
			}
			++i;
		}
		
		//calculates hashCode
		final int prime = 191;
		int tmpHashCode = 1;
		tmpHashCode = prime * tmpHashCode + Arrays.hashCode(args);
		tmpHashCode = prime * tmpHashCode + ((operator == null) ? 0 : operator.hashCode());
                tmpHashCode = prime * tmpHashCode + ((historyPoint == null) ? 0 : historyPoint.hashCode());
		this.hashCode = tmpHashCode;
		
		//calculates toString
		{
			final StringBuilder buf = new StringBuilder();
			buf.append(this.operator);
			buf.append('(');
			boolean first = true;
			for (Value v : this.args) {
				buf.append(first ? "" : ",");
				buf.append(v.toString());
				first = false;
			}
			buf.append(')');
			if (historyPoint != null) {
				buf.append('@');
				buf.append(historyPoint.toString());
			}
			this.toString = buf.toString();
		}
		
		//calculates originString
		{
            final StringBuilder buf = new StringBuilder();
            buf.append('<');
            buf.append(this.operator);
            buf.append('@');
            boolean first = true;
            for (Value v : this.args) {
                    buf.append(first ? "" : ",");
                    buf.append(v.isSymbolic() ? ((Symbolic) v).asOriginString() : v.toString());
                    first = false;
            }
            if (historyPoint() == null) {
                buf.append('>');
            } else {
                buf.append('@');
                buf.append(historyPoint().toString());
                buf.append('>');
            }
            this.originString = buf.toString();
		}
	}
	
	@Override
	public String getOperator() {
		return this.operator;
	}

	@Override
	public Value[] getArgs() {
		return this.args.clone();
	}
	
	@Override
	public String asOriginString() {
		return this.originString;
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
		v.visitPrimitiveSymbolicApply(this);
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
		final PrimitiveSymbolicApply other = (PrimitiveSymbolicApply) obj;
		if (!Arrays.equals(this.args, other.args)) {
			return false;
		}
		if (this.operator == null) {
			if (other.operator != null) {
				return false;
			}
		} else if (!this.operator.equals(other.operator)) {
			return false;
		}
		if (historyPoint() == null) {
		    if (other.historyPoint() != null) {
		        return false;
		    }
		} else if (!historyPoint().equals(other.historyPoint())) {
		    return false;
		}
		return true;
	}
}
