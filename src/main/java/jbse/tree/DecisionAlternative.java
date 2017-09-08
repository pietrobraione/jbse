package jbse.tree;

/**
 * Interface for all the decisions taken at symbolic execution 
 * branching bytecodes. All {@link DecisionAlternative}s are 
 * immutable.
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
	 * Checks whether the alternative resulted
	 * from a trivial decision, either because the 
	 * decision was taken by checking concrete values 
	 * (see {@link #concrete}), or because the same
	 * decision was taken earlier in the same trace. 
	 * Note that this is (currently) checked only for 
	 * symbolic references resolutions, thus in all 
	 * the sheer numeric decisions is 
	 * {@code trivial() == }{@link #concrete()}.
	 * 
	 * @return {@code true} iff the alternative 
	 * resulted from a trivial decision.
	 */
	boolean trivial(); //TODO move in Outcome
	
	/**
	 * Checks whether the alternative result
	 * from a decision on concrete values. 
	 * 
	 * @return {@code true} iff the alternative was
	 * taken by checking on concrete values. Note that
	 * {@code concrete()} implies
	 * {@link #trivial()}.
	 */
	boolean concrete(); //TODO move in Outcome
	
	/**
	 * Checks whether this alternative is the
	 * "no decision" one.
	 * 
	 * @return {@code true} iff the alternative was 
	 * taken without making any decision. Note that
	 * {@code noDecision()} implies
	 * {@link #concrete()}.
	 */
	boolean noDecision();
}
