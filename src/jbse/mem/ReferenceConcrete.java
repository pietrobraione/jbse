package jbse.mem;

import jbse.Type;

public class ReferenceConcrete extends Reference {
	/** The position in the heap denoted by this {@link Reference}. */
	private long pos;
	
    /**
     * Constructor returning a concrete reference to an object.
     * 
     * @param type a {@code char}, the type of the reference (see {@link Type}).
     * @param pos position of the object in the heap.
     */
    protected ReferenceConcrete(char type, long pos) {
    	super(type);
    	//TODO throw an exception if ref is invalid (especially necessary for null)
    	this.pos = pos;
    }
    
    /**
     * Constructor returning a concrete reference to an object.
     * 
     * @param pos position of the object in the heap.
     */
    public ReferenceConcrete(long pos) {
    	this(Type.REFERENCE, pos);
    }

	/**
	 * Checks whether this {@link Reference} denotes {@code null}. 
	 * 
	 * @return {@code true} iff {@code this} denotes {@code null}.
	 */
    public boolean isNull() {
    	return (this.pos == Util.POS_NULL);
    }

    public long getHeapPosition() {
    	return pos;
    }

    @Override
    public boolean isSymbolic() {
    	return false;
    }

    @Override
    public String toString() {
        return("Object[" + this.pos + "]");
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o == null) {
    		return false;
    	}
        boolean retVal = false;
        
        if (o instanceof ReferenceConcrete) {
            ReferenceConcrete r = (ReferenceConcrete) o;
            
          	retVal = (this.getHeapPosition() == r.getHeapPosition());
        }
        return retVal;
    }
    
    @Override
    public int hashCode() {
    	return (int) this.pos;
    }

	@Override
	public ReferenceConcrete clone() {
		return (ReferenceConcrete) super.clone();
	}
}
