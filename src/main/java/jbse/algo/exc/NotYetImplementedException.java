package jbse.algo.exc;

/**
 * Exception for an feature of JBSE that is not yet implemented.
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
    public NotYetImplementedException(String param) {
        super(param);
    }
}