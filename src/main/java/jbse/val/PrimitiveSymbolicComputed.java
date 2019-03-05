package jbse.val;

import jbse.common.exc.InvalidInputException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that represent a {@link PrimitiveSymbolic} value computed
 * from a set of other {@link Value}s. 
 */
public abstract class PrimitiveSymbolicComputed extends PrimitiveSymbolic {    
    /**
     * Constructor.
     * 
     * @param type the type of the represented value.
     * @param historyPoint the current {@link HistoryPoint}. It must not be {@code null}.
     * @throws InvalidTypeException if {@code type} is not primitive.
     * @throws InvalidInputException if {@code historyPoint == null}.
     */
    PrimitiveSymbolicComputed(char type, HistoryPoint historyPoint) 
    throws InvalidTypeException, InvalidInputException {
    	super(type, historyPoint);
    }
}