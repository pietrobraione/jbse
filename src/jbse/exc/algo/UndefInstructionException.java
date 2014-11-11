package jbse.exc.algo;

/**
 * Exception for undefined instruction algorithm.
 * 
 * @author Pietro Braione
 */
public class UndefInstructionException extends CannotManageStateException {
    /**
     * 
     */
    private static final long serialVersionUID = -4415922270426991464L;
    
    /**
     * Constructor
     */
    public UndefInstructionException(String param)
    {
        super(param);
    }
}