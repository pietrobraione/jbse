package jbse.val;

import jbse.common.Type;

/**
 * Class for symbolic {@link Reference}s to the {@link Heap}.
 * 
 * @author Pietro Braione
 */
public final class ReferenceSymbolic extends Reference implements Symbolic {
	/** An identifier for the value, in order to track lazy initialization. */
	private final int id;
	
    /** 
     * The static type of the reference. 
     */
    private final String staticType;

    /** The origin of the reference. */
	private final MemoryPath origin;
	
    /** The string representation of this object. */
	private final String toString;

    /**
     * Constructor returning an uninitialized symbolic reference.
     * 
     * @param id an {@code int} identifying the reference univocally.
     * @param staticType a {@link String}, the static type of the
     *        variable from which this reference originates (as 
     *        from {@code origin}).
     * @param origin a {@link MemoryPath}, the origin of this reference.
     */
    ReferenceSymbolic(int id, String staticType, MemoryPath origin) {
    	super(Type.REFERENCE);
        this.id = id;
        this.staticType = staticType;
        this.origin = origin;

        //calculates toString
        this.toString = "{R" + this.id + "}";
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
     * Returns the static type of this reference.
     * 
     * @return a {@code String}.
     */
    public String getStaticType() {
    	return this.staticType;
    }

    @Override
    public String getValue() {
    	return toString();
    }

    /**
     * {@inheritDoc}
     * For {@link ReferenceSymbolic} values it will always return {@code true}.
     */
    @Override
    public boolean isSymbolic() {
    	return true;
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
		final ReferenceSymbolic other = (ReferenceSymbolic) obj;
		return (this.id == other.id);
    }
    
    @Override
    public int hashCode() {
    	return this.id;
    }
    
    @Override
    public String toString() {
        return this.toString;
    }
}
