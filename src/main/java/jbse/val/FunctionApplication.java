package jbse.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Class of values representing the application of a pure
 * function from {@link Primitive}s to {@link Primitive}.
 * 
 * @author Pietro Braione
 */
public final class FunctionApplication extends Primitive {
    //pure functions implemented in java.lang.StrictMath 
    //TODO move them elsewhere? should make an enum? (no special advantage in both)

    /** Absolute value */
    public static final String ABS = "abs";

    /** Trigonometric sine */
    public static final String SIN = "sin";

    /** Trigonometric cosine */
    public static final String COS = "cos";

    /** Trigonometric tangent */
    public static final String TAN = "tan";

    /** Trigonometric arc sine */
    public static final String ASIN = "asin";

    /** Trigonometric arc cosine */
    public static final String ACOS = "acos";

    /** Trigonometric arc tangent */
    public static final String ATAN = "atan";

    /** Square root */
    public static final String SQRT = "sqrt";

    /** Power */
    public static final String POW = "pow";

    /** Exponential */
    public static final String EXP = "exp";

    /** Minimum */
    public static final String MIN = "min";

    /** Maximum */
    public static final String MAX = "max";

    /** The function name. */
    private final String operator;

    /** The args to which the function is applied, implicitly defining its arity. */
    private final Primitive[] args;

    /** The hash code of this object. */
    private final int hashCode;

    /** The string representation of this object. */
    private final String toString;

    /**
     * Constructor. 
     * 
     * @param type a {@code char}, the type of this {@link FunctionApplication}. 
     * @param calc a {@link Calculator}.
     * @param operator the name of the function.
     * @param args the {@link Primitive} arguments to which the function is applied.
     * @throws InvalidOperandException if any of {@code args} is null. 
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    public FunctionApplication(char type, Calculator calc, String operator, Primitive... args) 
    throws InvalidTypeException, InvalidOperandException {
        super(type, calc);
        this.operator = operator;
        this.args = args.clone();
        int i = 0;
        for (Primitive p : this.args) {
            if (p == null) {
                throw new InvalidOperandException(i + (i == 1 ? "-st" : i == 2 ? "-nd" : i == 3 ? "-rd ": "-th") + " argument is null");
            }
            ++i;
        }

        //calculates hash code
        final int prime = 191;
        int tmpHashCode = 1;
        tmpHashCode = prime * tmpHashCode + Arrays.hashCode(args);
        tmpHashCode = prime * tmpHashCode + ((operator == null) ? 0 : operator.hashCode());
        this.hashCode = tmpHashCode;

        //calculates toString
        final StringBuilder buf = new StringBuilder();
        buf.append(this.operator + "(");
        boolean first = true;
        for (Primitive p : this.args) {
            buf.append((first ? "" : ",") + p.toString());
            first = false;
        }
        buf.append(")");
        this.toString = buf.toString();
    }

    public String getOperator() {
        return this.operator;
    }

    public Primitive[] getArgs() {
        return this.args.clone();
    }

    public String getValue() {
        return toString();
    }

    /**
     * Returns the set of all the identifiers of symbolic 
     * values mentioned in this {@link FunctionApplication}.
     * 
     * @return a {@link Set}<code>&lt;</code>{@link Integer}<code>&gt;</code> of all and only the 
     *         identifiers of symbolic values mentioned 
     *         in this function application's args (empty when none).
     */
    public Set<Integer> symIds() {
        HashSet<Integer> retVal = new HashSet<Integer>();

        for (Primitive p : this.args) {
            if (p == null) {
                //nothing to do
            } else if (p instanceof PrimitiveSymbolic) {
                retVal.add(((PrimitiveSymbolic) p).getId());
            } else if (p instanceof Expression) {
                retVal.addAll(((Expression) p).symIds());
            } else if (p instanceof FunctionApplication) {
                retVal.addAll(((FunctionApplication) p).symIds());
            }
        }

        return retVal;
    }

    /**
     * {@inheritDoc}
     * For {@link FunctionApplication} objects it returns {@code false}.
     */
    @Override
    public boolean surelyTrue() {
        return false;
    }

    /**
     * {@inheritDoc}
     * For {@link FunctionApplication} objects it returns {@code false}.
     */
    @Override
    public boolean surelyFalse() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(PrimitiveVisitor v) throws Exception {
        v.visitFunctionApplication(this);
    }

    /**
     * {@inheritDoc}
     * For {@link FunctionApplication} objects it returns {@code true}.
     */
    @Override
    public boolean isSymbolic() {
        return true;
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
        final FunctionApplication other = (FunctionApplication) obj;
        if (!Arrays.equals(args, other.args))
            return false;
        if (operator == null) {
            if (other.operator != null) {
                return false;
            }
        } else if (!operator.equals(other.operator)) {
            return false;
        }
        return true;
    }
}
