package jbse.mem;

/**
 * Class that represents an instance in the heap whose
 * only purpose is to encapsulate an object at the 
 * meta-level.
 * 
 * @author Pietro Braione
 *
 */
public interface Instance_METALEVELBOX extends Instance {
	/**
	 * Gets the encapsulated object.
	 * 
	 * @return an {@link Object}.
	 */
	Object get();
	
	Instance_METALEVELBOX clone();
}
