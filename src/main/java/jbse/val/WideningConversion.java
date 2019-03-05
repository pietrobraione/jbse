package jbse.val;

import static jbse.val.HistoryPoint.unknown;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class representing the values resulting from a widening
 * conversion between numeric primitive types.
 * 
 * @author Pietro Braione
 *
 */
public final class WideningConversion extends PrimitiveSymbolicComputed {
    private final Primitive arg;
    private final String toString;
    private final String asOriginString;
    private final int hashCode;

    private WideningConversion(char type, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException, InvalidInputException {
        super(type, unknown()); //TODO put sensible history point?

        //checks on parameters
        if (arg == null) {
            throw new InvalidOperandException("Null operand in widening construction.");
        }
        if (!Type.widens(type, arg.getType())) {
            throw new InvalidTypeException("Cannot widen type " + arg.getType() + " to type " + type + ".");
        }
        
        this.arg = arg;

        //calculates toString
        this.toString = "WIDEN-"+ getType() + "(" + arg.toString() + ")";

        //calculates asOriginString
        this.asOriginString = "WIDEN-"+ getType() + "(" + (arg.isSymbolic() ? ((Symbolic) arg).asOriginString(): arg.toString()) + ")";;

        //calculates hashCode
        final int prime = 281;
        int result = 1;
        result = prime * result + arg.hashCode();
        result = prime * result + type;
        this.hashCode = result;
    }

    /**
     * Constructs a {@link WideningConversion}.
     * 
     * @param type a {@code char}, the destination type of the conversion.
     * @param arg a {@link Primitive}, the value that is being widened. 
     *        It must not be {@code null}.
     * @return a {@link WideningConversion}.
     * @throws InvalidOperandException if {@code arg == null}.
     * @throws InvalidTypeException if {@code arg} cannot be widened to {@code type},
     *         or {@code type} is not a valid primitive type.
     */
    public static WideningConversion make(char type, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException {
        try {
			return new WideningConversion(type, arg);
		} catch (InvalidInputException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }

    public Primitive getArg() {
        return this.arg;
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
        v.visitWideningConversion(this);
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
        final WideningConversion other = (WideningConversion) obj;
        if (this.getType() != other.getType()) {
        	return false;
        }
        if (!this.arg.equals(other.arg)) {
            return false;
        }
        return true;
    }
}
