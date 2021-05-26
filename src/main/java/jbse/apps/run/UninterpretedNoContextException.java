package jbse.apps.run;

/**
 * Thrown by a {@link DecisionProcedureGuidance} whenever
 * a method that was assumed pure (uninterpreted during 
 * symbolic execution) is invoked within a model code, 
 * where there is not a corresponding call context in the
 * concrete execution.
 * 
 * @author Pietro Braione
 *
 */
public final class UninterpretedNoContextException extends GuidanceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2707671905098000142L;

    public UninterpretedNoContextException() { super(); }

}

