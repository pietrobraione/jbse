package jbse.val;

import jbse.common.Type;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/03/18
 */
public abstract class Conversion extends Primitive {

    protected final Primitive arg;
    protected final String toString;
    protected final int hashCode;

    protected Conversion(char type, Calculator calc, Primitive arg)
            throws InvalidOperandException, InvalidTypeException {
        super(type, calc);

        //checks on parameters
        if (arg == null) {
            throw new InvalidOperandException("null operand in "+ this.conversionAsString() + "ing construction");
        }
        if (!Type.narrows(type, arg.getType())) {
            throw new InvalidTypeException("cannot " + this.conversionAsString() + " type " + arg.getType() + " to type " + type);
        }
        this.arg = arg;

        //calculates hashCode
        final int prime = getPrime();
        int result = 1;
        result = prime * result + arg.hashCode();
        result = prime * result + type;
        this.hashCode = result;

        //calculates toString
        this.toString = this.conversionAsString().toUpperCase() + "-" + this.getType() + "(" + arg.toString() + ")";
    }

    protected abstract String conversionAsString();

    protected abstract int getPrime();

    public Primitive getArg() {
        return this.arg;
    }

    @Override
    public boolean surelyTrue() {
        return false;
    }

    @Override
    public boolean surelyFalse() {
        return false;
    }

    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public Object getValueForNative() throws ValueDoesNotSupportNativeException {
        throw new ValueDoesNotSupportNativeException();
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
        final Conversion other = (Conversion) obj;
        if (arg == null) {
            if (other.arg != null) {
                return false;
            }
        } else if (!arg.equals(other.arg)) {
            return false;
        }
        return true;
    }

}
