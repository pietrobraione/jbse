package jbse.val;


/**
 * Interface that must be implemented by all the {@link Symbolic}
 * values that are applications of pure methods to a set of args.
 * 
 * @author Pietro Braione
 *
 */
public interface SymbolicApply extends Symbolic {
	/**
	 * Returns the operator, identifying the pure
	 * method that is being applied.
	 * 
	 * @return a {@link String}.
	 */
	String getOperator();
	
    /**
     * Returns the arguments of the pure method 
     * application.
     * 
     * @return a {@link Value}{@code []}, the 
     * arguments of the application. It is never 
     * {@code null}. 
     */
	Value[] getArgs();
}
