package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link ReferenceSymbolicRoot} whose origin is a 
 * local variable in the root frame. 
 */
public final class ReferenceSymbolicLocalVariable extends ReferenceSymbolicAtomic implements SymbolicLocalVariable {
    private final String variableName;
    private final String asOriginString;
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param variableName a {@link String}, the name of the local variable
     *        in the root frame this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param staticType a {@link String}, the static type of the
     *        reference (taken from bytecode).
     * @param historyPoint the current {@link HistoryPoint}.
     * @throws InvalidTypeException  if {@code staticType} is not an array or instance
	 *         reference type.
	 * @throws InvalidInputException if {@code staticType == null || historyPoint == null || variableName == null}.
     */
    ReferenceSymbolicLocalVariable(String variableName, int id, String staticType, HistoryPoint historyPoint) throws InvalidInputException, InvalidTypeException {
    	super(id, staticType, historyPoint);
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