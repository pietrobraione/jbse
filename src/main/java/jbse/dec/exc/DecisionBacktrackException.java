package jbse.dec.exc;


public class DecisionBacktrackException extends DecisionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7349302034836929909L;
	
	public DecisionBacktrackException(DecisionException e) {
		super(e.getMessage());
	}

}
