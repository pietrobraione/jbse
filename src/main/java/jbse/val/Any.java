package jbse.val;

import static jbse.val.HistoryPoint.unknown;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.PrimitiveSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * The boolean value on the top of the information lattice of booleans.
 * It always have either {@code true} or {@code false} as value and 
 * no further assumption on it can be done. It is used to model 
 * nondeterminism.
 * 
 * @author Pietro Braione
 */
public final class Any extends PrimitiveSymbolic {
    private Any(Calculator calc) throws InvalidTypeException {
        super(Type.BOOLEAN, unknown(), calc);
    }

    public static Any make(Calculator calc) {
        try {
            return new Any(calc);
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    @Override
    public String asOriginString() {
        return "*";
    }

    @Override
    public void accept(PrimitiveVisitor v) throws Exception { 
        v.visitAny(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o; //TODO should all any's be equal? Now they are all different.
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "*";
    }
}
