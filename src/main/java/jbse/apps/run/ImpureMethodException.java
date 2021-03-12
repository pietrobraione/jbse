package jbse.apps.run;

/**
 * Internally thrown in a {@link DecisionProcedureGuidance} whenever
 * a method that was assumed pure (uninterpreted during symbolic execution)
 * is discovered to be impure by concrete execution.
 * 
 * @author Pietro Braione
 *
 */
public final class ImpureMethodException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4543125459676530865L;
	
}

