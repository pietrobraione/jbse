package jbse.mem;

/**
 * A path condition's clause. 
 *
 * @author Pietro Braione
 *
 */
public interface Clause extends Cloneable {
	Clause clone() throws CloneNotSupportedException;
	void accept(ClauseVisitor v) throws Exception;
}