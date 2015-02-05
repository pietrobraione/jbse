package jbse.common.exc;

/**
 * This exception is thrown whenever the JRE is not on 
 * the classpath or is incompatible.
 *  
 * @author Pietro Braione
 *
 */
public class ClasspathException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -8553852699901572140L;

    public ClasspathException() {
        super();
    }

    public ClasspathException(String message) {
        super(message);
    }

    public ClasspathException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClasspathException(Throwable cause) {
        super(cause);
    }
}
