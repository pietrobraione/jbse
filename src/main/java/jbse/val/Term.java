package jbse.val;

import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * An arbitrary {@link Primitive} value, on which 
 * assumptions can be made. Terms serve essentially two purposes: They
 * are used to represent internal or bound variables, which are not 
 * meant to be used externally but rather replaced by some other
 * {@link Primitive} (e.g., a term is used to represent the generic array
 * index, but is usually replaced with the concrete value used to access
 * the array); And they are used for interfacing the symbolic executor 
 * with an external decision procedure whenever a {@link Primitive} cannot 
 * be directly sent (e.g., some pure function applications cannot be 
 * handled by some decision procedures, and are therefore consistently
 * replaced by suitable terms). <br />
 * 
 * Terms are symbolic but do not implement the Symbolic interface. This 
 * because they are not identified by the state: A {@link String} name 
 * must be provided upon construction, and the name 
 * identifies them. A term stands for itself and is never replaced by 
 * another term. Also, a term has no origin.
 * 
 * @author Pietro Braione
 */
public final class Term extends Primitive {
    /** The conventional value of the {@link Term}, a {@link String}. */
	private final String value;
	
	/** Cached hash code. */
	private final int hashCode;
	
	/**
	 * Constructor.
	 * 
	 * @param type
	 * @param calc
	 * @param value
	 * @throws InvalidTypeException 
	 */
	Term(char type, Calculator calc, String value) throws InvalidTypeException {
		super(type, calc);
		this.value = value;
		
		//calculates the hash code
		final int prime = 293;
		int tmpHashCode = 1;
		tmpHashCode = prime * tmpHashCode + ((value == null) ? 0 : value.hashCode());
		this.hashCode = tmpHashCode;
	}

    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public void accept(PrimitiveVisitor v) throws Exception {
		v.visitTerm(this);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean surelyTrue() {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean surelyFalse() {
		return false;
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
	public boolean isSymbolic() {
		return true;
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
		Term other = (Term) obj;
		if (value == null) {
			if (other.value != null) { 
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String toString() {
		return this.value;
	}
}
