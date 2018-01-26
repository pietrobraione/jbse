package jbse.bc.exc;

/**
 * Exception thrown in all the situations (e.g., class resolution)
 * where a class definition is not found. 
 * Typically a {@link ClassNotFoundException}, possibly boxed in a {@link java.lang.NoClassDefFoundError}, 
 * must be raised.
 * 
 * @author Pietro Braione
 *
 */
public class ClassFileNotFoundException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -676901846456659964L;

	public ClassFileNotFoundException(String param) {
		super(param);
    }
}
