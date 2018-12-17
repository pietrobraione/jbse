package jbse.val;

import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolicAtomic} whose origin is a 
 * local variable in the root frame. 
 */
public final class PrimitiveSymbolicLocalVariable extends PrimitiveSymbolicAtomic implements SymbolicLocalVariable {
    final String variableName;
    
    /**
     * Constructor.
     * 
     * @param variableName a {@link String}, the name of the local variable
     *        in the root frame this symbol originates from.
     * @param id an {@link int}, the identifier of the symbol. Different
     *        object with same identifier will be treated as equal.
     * @param type the type of the represented value.
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws NullPointerException if {@code calc == null || historyPoint == null}.
     */
    PrimitiveSymbolicLocalVariable(String variableName, int id, char type, HistoryPoint historyPoint, Calculator calc) throws InvalidTypeException {
    	super(id, type, historyPoint, calc);
    	this.variableName = variableName;
    }
    
    @Override
    public String getVariableName() {
        return this.variableName;
    }
    
    @Override
    public String asOriginString() {
        return "{ROOT}:" + this.getVariableName();
    }
}