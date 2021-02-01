package jbse.val;

import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.TYPEVAR;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicMember} whose origin is an entry
 * in a map (key slot). 
 */
public final class ReferenceSymbolicMemberMapKey extends ReferenceSymbolicMember {
	/** The key origin specifier. */
	private final String keyOriginSpecifier;
	
    /** The origin String representation of this object. */
    private final String asOriginString;
    
	/** The hash code of this object. */
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer a map.
     * @param keyOriginSpecifier a {@link String}. It will be used to specify
     *        the origin string representation of this symbol, that will be 
     *        built as {@code container.}{@link ReferenceSymbolic#asOriginString() asOriginString}{@code () + "::" + keyOriginSpecifier}.
     *        It is used to disambiguate the identity of references with same
     *        {@code container}.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException if {@code key == null || historyPoint == null}.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberMapKey(ReferenceSymbolic container, String keyOriginSpecifier, int id) throws InvalidInputException, InvalidTypeException {
    	super(container, id, REFERENCE + JAVA_OBJECT + TYPEEND, TYPEVAR + "K" + TYPEEND);
    	if (keyOriginSpecifier == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicMemberMapValue with null key.");
    	}
    	
    	this.keyOriginSpecifier = keyOriginSpecifier;
    	this.asOriginString = getContainer().asOriginString() + "::" + this.keyOriginSpecifier;

    	//calculates hashCode
		final int prime = 131111;
		int result = 1;
		result = prime * result + getContainer().hashCode();
		result = prime * result + this.keyOriginSpecifier.hashCode();
		this.hashCode = result;
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
		final ReferenceSymbolicMemberMapKey other = (ReferenceSymbolicMemberMapKey) obj;
		if (!getContainer().equals(other.getContainer())) {
			return false;
		}
		if (!this.keyOriginSpecifier.equals(other.keyOriginSpecifier)) {
			return false;
		}
		return true;
	}
}