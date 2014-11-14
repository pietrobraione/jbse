package jbse.val;


/**
 * Interface that must be implemented by all the symbolic {@link Value}s.
 * 
 * @author Pietro Braione
 *
 */
public interface Symbolic {
	/**
	 * Returns the origin of the symbol, i.e., 
	 * the sequence of fields (path) from 
	 * the root through which the symbol was 
	 * first discovered.
	 * 
	 * @return a {@link String}.
	 */
	String getOrigin();

	/**
	 * Returns the identifier of the symbol.
	 * 
	 * @return a {@code int}, the identifier 
	 * of the symbol. Two different symbols 
	 * can have same identifier only if they
	 * belong to different classes. 
	 */
	int getId();
}
