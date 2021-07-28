package jbse.val;

import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.TYPEVAR;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is an entry
 * in a map (value slot). It may originate only in an initial symbolic map by a
 * refinement.
 */
public final class ReferenceSymbolicMemberMapValue extends ReferenceSymbolicMember {
	/** The {@link Reference} to the key object associated to the value. */
    private final Reference key;
    
    /** The {@link HistoryPoint} of {@link #key} (to identify its state). */
    private final HistoryPoint keyHistoryPoint;
    
    /** The origin String representation of this object. */
    private final String asOriginString;
    
	/** The hash code of this object. */
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer an initial map.
     * @param key a {@link Reference}, the key of the entry in the 
     *        container map this symbol originates from. It must not be {@code null}.
     * @param keyHistoryPoint the {@link HistoryPoint} of {@code key} (to identify its 
     *        state). If {@code null}, the history point
     *        is assumed to be that of {@code container}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException if {@code key == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberMapValue(ReferenceSymbolic container, Reference key, HistoryPoint keyHistoryPoint, int id) 
    throws InvalidInputException, InvalidTypeException {
    	super(container, id, REFERENCE + JAVA_OBJECT + TYPEEND, TYPEVAR + "V" + TYPEEND);
    	if (key == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberMapValue with null key.");
    	}
    	
    	this.key = key;
    	this.keyHistoryPoint = (keyHistoryPoint == null ? container.historyPoint() : keyHistoryPoint);
    	this.asOriginString = getContainer().asOriginString() + "::GET[" + (this.key.isSymbolic() ? ((Symbolic) this.key).asOriginString() : this.key.toString()) + "@" + keyHistoryPoint.toString() + "]";

    	//calculates hashCode
		final int prime = 131071;
		int result = 1;
		result = prime * result + getContainer().hashCode();
		result = prime * result + key.hashCode();
		result = prime * result + keyHistoryPoint.hashCode();
		this.hashCode = result;
    }

    /**
     * Returns the {@link Reference} to the key object associated 
     * to this value slot {@link Reference}.
     * 
     * @return a {@link Reference}.
     */
    public Reference getAssociatedKey() {
        return this.key;
    }
    
    /**
     * Returns the key {@link HistoryPoint}.
     * 
     * @return the {@link HistoryPoint} of the
     *         associated key object, allowing 
     *         to reconstruct its state when it 
     *         was observed as a map key associated
     *         to this value.
     */
    public HistoryPoint keyHistoryPoint() {
		return this.keyHistoryPoint;
	}
    
    @Override
    public String asOriginString() {
        return this.asOriginString;
    }
    
    @Override
    public void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceSymbolicMemberMapValue(this);
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
		final ReferenceSymbolicMemberMapValue other = (ReferenceSymbolicMemberMapValue) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (!this.key.equals(other.key)) {
			return false;
		}
		if (!this.keyHistoryPoint.equals(other.keyHistoryPoint)) {
			return false;
		}
		return true;
	}
}