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
    /** The origin String representation of this object. */
    private final String asOriginString;
    
    /** The hash code of this object. */
    private final int hashCode;
    
    /**
     * Constructor.
     * 
     * @param container a {@link ReferenceSymbolic}, the container object
     *        this symbol originates from. It must refer a map.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        objects with same identifiers will be treated as equal.
     * @throws InvalidTypeException never.
     * @throws InvalidInputException never.
     * @throws NullPointerException if {@code container == null}.
     */
    ReferenceSymbolicMemberMapKey(ReferenceSymbolic container, int id) throws InvalidInputException, InvalidTypeException {
    	super(container, id, REFERENCE + JAVA_OBJECT + TYPEEND, TYPEVAR + "K" + TYPEEND);
    	this.asOriginString = getContainer().asOriginString() + "::KEY(" + id + ")";

    	//calculates hashCode
		this.hashCode = System.identityHashCode(this);
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
    	return this == obj;
    }
}
