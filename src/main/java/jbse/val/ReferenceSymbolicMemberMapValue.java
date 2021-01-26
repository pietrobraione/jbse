package jbse.val;

import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.TYPEVAR;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is an entry
 * in a map (value slot). 
 */
public final class ReferenceSymbolicMemberMapValue extends ReferenceSymbolicMember {
	/** The {@link Reference} to the key object associated to the value. */
    private final Reference key;
    
    /** The current {@link HistoryPoint} (to disambiguate the state of {@link #key}). */
    private final HistoryPoint historyPoint;
    
    /** The origin String representation of this object. */
    private final String asOriginString;
    
	/** The hash code of this object. */
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer a map.
     * @param key a {@link Reference}, the key of the entry in the 
     *        container map this symbol originates from. It must not be {@code null}.
     * @param historyPoint the current {@link HistoryPoint}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException if {@code key == null || historyPoint == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberMapValue(ReferenceSymbolic container, Reference key, HistoryPoint historyPoint, int id) throws InvalidInputException, InvalidTypeException {
    	super(container, id, REFERENCE + JAVA_OBJECT + TYPEEND, TYPEVAR + "V" + TYPEEND);
    	if (key == null || historyPoint == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberMapValue with null key.");
    	}
    	
    	this.key = key;
    	this.historyPoint = historyPoint;
    	this.asOriginString = getContainer().asOriginString() + "::GET[" + (this.key.isSymbolic() ? ((Symbolic) this.key).asOriginString() : this.key.toString()) + "@" + historyPoint.toString() + "]";

    	//calculates hashCode
		final int prime = 131071;
		int result = 1;
		result = prime * result + getContainer().hashCode();
		result = prime * result + key.hashCode();
		this.hashCode = result;
    }

    /**
     * Returns the {@link Reference} to the key object associated 
     * to this value slot {@link Reference}.
     * 
     * @return a {@link Reference}.
     */
    public Reference getKey() {
        return this.key;
    }
    
    /**
     * Returns the {@link HistoryPoint}.
     * 
     * @return a {@link HistoryPoint}.
     */
    public HistoryPoint getHistoryPoint() {
		return this.historyPoint;
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
		return true;
	}
}