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
    private final Reference key;
    private final String asOriginString;
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer a map.
     * @param key a {@link Reference}, the key of the entry in the 
     *        container map this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        objects with same identifiers will be treated as equal.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException if {@code key == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberMapValue(ReferenceSymbolic container, Reference key, int id) throws InvalidInputException, InvalidTypeException {
    	super(container, id, REFERENCE + JAVA_OBJECT + TYPEEND, TYPEVAR + "V" + TYPEEND);
    	if (key == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberMapValue with null key.");
    	}
    	
    	this.key = key;
    	this.asOriginString = getContainer().asOriginString() + ".get(" + (this.key.isSymbolic() ? ((Symbolic) this.key).asOriginString() : this.key.toString()) + ")";

    	//calculates hashCode
		final int prime = 131071;
		int result = 1;
		result = prime * result + getContainer().hashCode();
		result = prime * result + key.hashCode();
		this.hashCode = result;
    }

    public Reference getKey() {
        return this.key;
    }
    
    @Override
    public String asOriginString() {
        return this.asOriginString;
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