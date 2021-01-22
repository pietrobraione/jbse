package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicRoot} whose origin is a 
 * local variable in the root frame. 
 */
public final class ReferenceSymbolicLocalVariable extends ReferenceSymbolicAtomic implements SymbolicLocalVariable {
	/**
	 * The name of the local variable in the root 
	 * frame this symbol originates from.	 
	 */
    private final String variableName;
    
    /** The origin String representation of this object. */
    private final String asOriginString;
    
    /** The hash code of this object. */
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param variableName a {@link String}, the name of the local variable
     *        in the root frame this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param genericSignatureType a {@link String}, the generic signature 
     *        type of the reference (taken from bytecode, its type erasure
     *        must be {@code staticType}).
     * @param historyPoint the current {@link HistoryPoint}.
     * @throws InvalidTypeException  if {@code staticType} is not an array or instance
	 *         reference type.
	 * @throws InvalidInputException if {@code staticType == null || genericSignatureType == null || historyPoint == null || variableName == null}.
     */
    ReferenceSymbolicLocalVariable(String variableName, int id, String staticType, String genericSignatureType, HistoryPoint historyPoint) throws InvalidInputException, InvalidTypeException {
    	super(id, staticType, genericSignatureType, historyPoint);
    	if (variableName == null) {
    		throw new InvalidInputException("Attempted the creation of a ReferenceSymbolicLocalVariable with null variableName.");
    	}
    	this.variableName = variableName;
    	this.asOriginString = "{ROOT}:" + this.variableName;
    	
    	//calculates hashCode
		final int prime = 5227;
		int result = 1;
		result = prime * result + variableName.hashCode();
		this.hashCode = result;
    }

	@Override
    public final String getVariableName() {
        return this.variableName;
    }
    
    @Override
    public String asOriginString() {
        return this.asOriginString;
    }
    
    @Override
    public ReferenceSymbolic root() {
    	return this;
    }
    
    @Override
    public boolean hasContainer(Symbolic s) {
		if (s == null) {
			throw new NullPointerException();
		}
		return equals(s);
    }
    
    @Override
    public void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceSymbolicLocalVariable(this);
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
		final ReferenceSymbolicLocalVariable other = (ReferenceSymbolicLocalVariable) obj;
		if (!this.variableName.equals(other.variableName)) {
			return false;
		}
		return true;
	}
}