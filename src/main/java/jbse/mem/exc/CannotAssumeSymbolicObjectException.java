package jbse.mem.exc;

import jbse.algo.exc.CannotManageStateException;

/**
 * Exception thrown whenever the assumption of the existence of 
 * a symbolic object in the initial heap fails. This currently
 * applies whenever one tries to assume the existence of a 
 * {@code java.lang.Class} or {@code java.lang.ClassLoader} 
 * object.
 * 
 * @author Pietro Braione
 */
public class CannotAssumeSymbolicObjectException extends CannotManageStateException {
    public CannotAssumeSymbolicObjectException(String msg) {
        super(msg);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3857577059863452424L;
}
