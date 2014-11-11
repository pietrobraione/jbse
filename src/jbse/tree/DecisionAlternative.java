package jbse.tree;

/**
 * Interface for all the outcomes of state splitting decisions.
 * 
 * @author Pietro Braione
 */
public interface DecisionAlternative {
	/**
	 * Returns an identifier for the decision alternative.
	 * 
	 * @return a {@link String}.
	 */
	String getIdentifier();
	
	/**
	 * Returns a branch number for the decision alternative.
	 * 
	 * @return an {@code int}.
	 */
	int getBranchNumber();
	
	/**
	 * Checks whether the alternative has been 
	 * trivially decided. 
	 * 
	 * @return {@code true} iff the alternative 
	 * resulted from a trivial decision.
	 */
	boolean trivial();
	
	/**
	 * Checks whether the alternative derived
	 * from checking on concrete values. 
	 * 
	 * @return {@code true} iff the alternative derived
	 * from checking on concrete values. Note that
	 * {@code this.concrete() == true} implies
	 * {@code this.trivial() == true}.
	 */
	boolean concrete();
}
