package jbse.val;

import static jbse.val.HistoryPoint.unknown;

import jbse.common.Type;
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

    private WideningConversion(char type, Calculator calc, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException {
        super(type, unknown(), calc);

        //checks on parameters
        if (arg == null) {
            throw new InvalidOperandException("Null operand in widening construction");
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

    public static WideningConversion make(char type, Calculator calc, Primitive arg) 
    throws InvalidOperandException, InvalidTypeException {
        return new WideningConversion(type, calc, arg);
    }

    public Primitive getArg() {
        return this.arg;
    }
    
    @Override
    public Primitive doReplace(Primitive from, Primitive to) {
    	final Primitive newArg;
    	if (this.arg.equals(from)) {
    		newArg = to;
    	} else if (this.arg instanceof PrimitiveSymbolicComputed) {
    		newArg = ((PrimitiveSymbolicComputed) this.arg).doReplace(from, to);
    	} else {
    		newArg = this.arg;
    	}
    	
    	try {
			return this.calc.widen(getType(), newArg);
		} catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
		}
    }

	@Override
	public String asOriginString() {
		return this.asOriginString;
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
        if (!this.arg.equals(other.arg)) {
            return false;
        }
        return true;
    }
}
