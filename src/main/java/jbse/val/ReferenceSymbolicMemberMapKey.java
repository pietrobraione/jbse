package jbse.val;

import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.TYPEVAR;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is an entry
 * in a map (key slot). It may originate only in an initial symbolic map by a
 * refinement.
 */
public final class ReferenceSymbolicMemberMapKey extends ReferenceSymbolicMember {
	/** 
	 * The {@link Reference} to the value object associated to this key.
	 * Not final because of circularity. 
	 */
	private Reference value;
	
    /** The {@link HistoryPoint} of {@link #value} (to identify its state). */
	private final HistoryPoint valueHistoryPoint;
	
	/** The identifier. */
	private final int id;
	
    /** The origin String representation of this object. */
    private final String asOriginString;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer an initial map.
     * @param value a {@link Reference}, the value of the entry in the 
     *        container this symbol originates from. It can be {@code null}, 
     *        in such case it can be set later with the {@link #setAssociatedValue(Reference) setAssociatedValue}
     *        method.
     * @param valueHistoryPoint the {@link HistoryPoint} of {@code value} 
     *        (to identify its state). If {@code null}, the history point
     *        is assumed to be that of {@code container}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol, and to disambiguate
     *        different keys corresponding to identical values.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException never.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberMapKey(ReferenceSymbolic container, Reference value, HistoryPoint valueHistoryPoint, int id) 
    throws InvalidInputException, InvalidTypeException {
    	super(container, id, REFERENCE + JAVA_OBJECT + TYPEEND, TYPEVAR + "K" + TYPEEND);
    	
    	this.value = value;
    	this.valueHistoryPoint = (valueHistoryPoint == null ? container.historyPoint() : valueHistoryPoint);
    	this.id = id;
    	if (this.value == null) {
    		this.asOriginString = getContainer().asOriginString() + "::KEY[" + this.id + "]";
    	} else {
    		this.asOriginString = getContainer().asOriginString() + "::KEY-OF[" + (this.value.isSymbolic() ? ((Symbolic) this.value).asOriginString() : this.value.toString()) + "@" + this.valueHistoryPoint.toString() + ", " + this.id + "]";
    	}
    }
    
    /**
     * Sets the {@link Reference} to the value object associated
     * to this key. It allows to set the value only once, after
     * it was left null by the constructor, to a nonnull value. 
     * Then, it cannot be further modified.
     * 
     * @param value a {@link Reference}.
     */
    public void setAssociatedValue(Reference value) {
    	if (this.value == null) {
    		this.value = value;
    	}
    }
    
    /**
     * Returns the {@link Reference} to the value object associated 
     * to this key.
     * 
     * @return a {@link Reference}.
     */
    public Reference getAssociatedValue() {
    	return this.value;
    }
    
    /**
     * Returns the value {@link HistoryPoint}.
     * 
     * @return the {@link HistoryPoint} of the
     *         associated value object, allowing 
     *         to reconstruct its state when it 
     *         was observed as a map value of 
     *         this key.
     */
    public HistoryPoint valueHistoryPoint() {
    	return this.valueHistoryPoint;
    }

    @Override
    public String asOriginString() {
        return this.asOriginString;
    }
    
    @Override
    public void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceSymbolicMemberMapKey(this);
    }
    
    @Override
    public int hashCode() {
    	final int prime = 131111;
    	int result = 1;
    	result = prime * result + getContainer().hashCode();
    	result = prime * result + (this.value == null ? 0 : this.value.hashCode());
    	result = prime * result + this.valueHistoryPoint.hashCode();
    	result = prime * result + this.id;
    	return result;
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
		final ReferenceSymbolicMemberMapKey other = (ReferenceSymbolicMemberMapKey) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (this.value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!this.value.equals(other.value)) {
			return false;
		}
		if (!this.valueHistoryPoint.equals(other.valueHistoryPoint)) {
			return false;
		}
		if (this.id != other.id) {
			return false;
		}
		return true;
	}
}