package jbse.algo.meta.exc;

import jbse.algo.exc.CannotManageStateException;

/**
 * Exception thrown whenever an unsafe operation produces an undefined result.
 * 
 * @author Pietro Braione
 */
public class UndefinedResultException extends CannotManageStateException {
    /**
     * 
     */
    private static final long serialVersionUID = -4352182698283195143L;

    public UndefinedResultException() { 
        super(); 
    }

    public UndefinedResultException(String s) { 
        super(s); 
    }

    public UndefinedResultException(Throwable e) { 
        super(e); 
    }

    public UndefinedResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedResultException(String message, Throwable cause,
                                    boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
