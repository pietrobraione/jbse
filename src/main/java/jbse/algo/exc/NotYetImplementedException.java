package jbse.algo.exc;

/**
 * Exception for an undefined bytecode.
 * 
 * @author Pietro Braione
 */
public class NotYetImplementedException extends CannotManageStateException {
    /**
     * 
     */
    private static final long serialVersionUID = -4415922270426991464L;
    
    /**
     * Constructor
     */
    public NotYetImplementedException(String param)
    {
        super(param);
    }
}