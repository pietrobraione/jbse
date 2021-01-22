package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolic} atomic 
 * (non computed) value. 
 */
public abstract class PrimitiveSymbolicAtomic extends PrimitiveSymbolic implements SymbolicAtomic {    
    /** The string representation of this object. */
    private final String toString;
    
    /**
     * Constructor.
     * 
     * @param id an {@link int}, the identifier of the symbol. Used only
     *        in the toString representation of the symbol.
     * @param type the type of the represented value.
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code historyPoint == null}.
     */
    PrimitiveSymbolicAtomic(int id, char type, HistoryPoint historyPoint) 
    throws InvalidTypeException, InvalidInputException {
    	super(type, historyPoint);
        
    	//calculates toString
    	this.toString = "{V" + id + "}";
    }

    @Override
    public final void accept(PrimitiveVisitor v) throws Exception {
        v.visitPrimitiveSymbolicAtomic(this);
    }

    @Override
    public final String toString() {
        return this.toString;
    }
}