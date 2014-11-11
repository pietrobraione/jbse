package jbse.mem;

import jbse.Type;

/**
 * Class for symbolic {@link Reference}s to the {@link Heap}.
 * 
 * @author Pietro Braione
 */
public class ReferenceSymbolic extends Reference implements Symbolic {
	/** An identifier for the value, in order to track lazy initialization. */
	private final int id;
	
    /** 
     * The static type of the symbolic reference, or {@code null} 
     * iff it is a concrete reference. 
     */
    private final String staticType;

    /** The origin of the reference. */
	private final String origin;

    /**
     * Constructor returning an uninitialized symbolic reference.
     */
    ReferenceSymbolic(int id, String staticType, String origin) {
    	super(Type.REFERENCE);
        this.id = id;
        this.staticType = staticType;
        this.origin = origin;
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
    public String getOrigin() {
    	return this.origin;
    }

    public String getStaticType() {
    	return this.staticType;
    }

    public String getValue() {
    	return "{R" + this.id + "}";
    }

    @Override
    public boolean isSymbolic() {
    	return true;
    }
    
    @Override
    public String toString() {
    	return this.getValue();
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
	public ReferenceSymbolic clone() {
		return (ReferenceSymbolic) super.clone();
	}
}
