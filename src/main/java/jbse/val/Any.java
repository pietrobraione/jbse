package jbse.val;

import static jbse.val.HistoryPoint.unknown;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
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
	private static final Any THE_ANY;
	static {
        try {
        	THE_ANY = new Any();
        } catch (InvalidTypeException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
	}
	
    private Any() throws InvalidTypeException, InvalidInputException {
        super(Type.BOOLEAN, unknown());
    }

    /**
     * Makes an {@link Any} value.
     * 
     * @return an {@link Any} instance.
     */
    public static Any make() {
    	return THE_ANY;
    }
    
    @Override
    public String asOriginString() {
        return "*";
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
