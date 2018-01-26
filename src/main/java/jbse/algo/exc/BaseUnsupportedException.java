package jbse.algo.exc;

/**
 * Exception raised whenever the base-level overriding of 
 * a method fails. 
 *  
 * @author Pietro Braione
 *
 */
public class BaseUnsupportedException extends CannotManageStateException {

    /**
     * 
     */
    private static final long serialVersionUID = 9181624209704832999L;

    public BaseUnsupportedException(String param) {
        super(param);
    }
    
    public BaseUnsupportedException(Throwable e) {
        super(e);
    }
}
