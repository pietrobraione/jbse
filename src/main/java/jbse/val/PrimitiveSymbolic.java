package jbse.val;

import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;


/**
 * Class that represent a symbolic value. 
 */
public final class PrimitiveSymbolic extends Primitive implements Symbolic {
	/** The identifier of this symbol. */
    private final int id;
    
    /** The hash code. */
    private final int hashCode;
    
    /** The string representation of this object. */
    private final String toString;

    /** The origin. */
    private final MemoryPath origin;
    
    /**
     * Constructor.
     * 
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param type the type of the represented value.
     * @param origin a {@link MemoryPath}, the origin of the symbol.
     * @param calc a {@link Calculator}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     */
    PrimitiveSymbolic(int id, char type, MemoryPath origin, Calculator calc) throws InvalidTypeException {
    	super(type, calc);
    	this.id = id;
        this.origin = origin;
        
        //calculates the hash code
		final int prime = 89;
		int result = 1;
		result = prime * result + id;
		this.hashCode = result;
		
		//calculates toString
        this.toString = "{V" + this.id + "}";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
    	return this.id;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MemoryPath getOrigin() {
    	return this.origin;
    }
    
    /**
     * Returns the value of symbolic type.
     */
    @Override
    public String getValue() {
    	return toString();
    }

    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will always return {@code true}.
     */
	@Override
	public boolean isSymbolic() {
		return true;
	}

    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will always throw {@link ValueDoesNotSupportNativeException}.
     */
	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}

    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will return {@code false}.
     */
	@Override
	public boolean surelyTrue() {
		return false;
	}
    
    /**
     * {@inheritDoc}
     * For {@link PrimitiveSymbolic} values it will return {@code false}.
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
		v.visitPrimitiveSymbolic(this);
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
     * Two {@link PrimitiveSymbolic} values are equal iff they
     * have same identifier.
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
		PrimitiveSymbolic other = (PrimitiveSymbolic) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.toString;
    }
}