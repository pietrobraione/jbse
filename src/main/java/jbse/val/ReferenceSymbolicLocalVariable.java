package jbse.val;

/**
 * Class that represent a {@link ReferenceSymbolicRoot} whose origin is a 
 * local variable in the root frame. 
 */
public final class ReferenceSymbolicLocalVariable extends ReferenceSymbolicAtomic implements SymbolicLocalVariable {
    private final String variableName;
    private final String asOriginString;

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
     */
    ReferenceSymbolicLocalVariable(String variableName, int id, String staticType, HistoryPoint historyPoint) {
    	super(id, staticType, historyPoint);
    	this.variableName = variableName;
    	this.asOriginString = "{ROOT}:" + this.variableName;
    }
    
    @Override
    public ReferenceSymbolic root() {
    	return this;
    }

    @Override
    public final String getVariableName() {
        return this.variableName;
    }
    
    @Override
    public String asOriginString() {
        return this.asOriginString;
    }
}